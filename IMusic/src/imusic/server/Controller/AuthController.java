package imusic.server.Controller;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import imusic.server.Controller.utils.JWTSigner;
import imusic.server.Controller.utils.JWTVerifier;
import imusic.server.Controller.utils.JWTVerifyException;
import imusic.server.aop.PostMethod;
import imusic.server.aop.UserAuth;
import imusic.server.config.CacheIniter;
import imusic.server.config.MainConfig;
import imusic.server.models.Musician;
import imusic.server.models.Photo;
import imusic.server.models.Song;
import imusic.server.models.User;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Before(UserAuth.class)
public class AuthController extends Controller {
    private final static String MD5(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Clear(UserAuth.class)
    public void login() {
        this.login(null, null);
    }
    @Clear(UserAuth.class)
    public void login(String user, String pass) {
        Result result = new Result();
        if (user == null && pass == null) {
            user = getPara("username");
            pass = getPara("password");
        }
        User a = User.dao.findFirst(
                "SELECT * FROM user WHERE username =? AND password =?", user,
                pass);
        if (a == null) {
            result.put("Code", 103);
            result.put("Msg", "Wrong Username Or Password");
            renderJson(result);
            return;
        }
        if (a.getInt("enable") == 2) {
            result.put("Code", 104);
            result.put("Msg", "Your Count Is DISABLED");
            renderJson(result);
            return;
        } else {
            JWTSigner signer = new JWTSigner("SuperMTNeverFallBack");
            Map<String, Object> claims = new HashMap<String, Object>();
            String token = a.getStr("token");
            if (token == null) {
                claims.put("userid", a.getInt("id"));
                claims.put("WEB", "offline");
                claims.put("UWP", "offline");
                claims.put("IOS", "offline");
                claims.put("ADW", "offline");
            } // token为空，所有客户端都未登陆过
            else {
                JWTVerifier verfier = new JWTVerifier("SuperMTNeverFallBack");
                Map temp = null;
                try {
                    temp = verfier.verify(token);
                    claims = temp;
                    claims.put("userid", a.getInt("id"));
                } catch (InvalidKeyException | NoSuchAlgorithmException
                        | IllegalStateException | SignatureException
                        | IOException | JWTVerifyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (this.getRequest().getHeader("agent") != null) {
                if (this.getRequest().getHeader("agent").equals("UWP"))
                    claims.put("UWP", "online");
                if (this.getRequest().getHeader("agent").equals("IOS"))
                    claims.put("IOS", "online");
                if (this.getRequest().getHeader("agent").equals("ADW"))
                    claims.put("ADW", "online");
            } else {
                claims.put("WEB", "online");
                claims.put("UWP", "offline");
                claims.put("ADW", "offline");
                claims.put("IOS", "offline");
            }
            String pretoken = signer.sign(claims);
            a.set("token", pretoken);
            a.set("logincount", String.valueOf(a.getInt("logincount") + 1));
            a.set("lastlogin", new Timestamp(System.currentTimeMillis()));
            a.update();
            System.out.println(claims.toString());
            if (a.getInt("enable") != 1) {
                result.put("Code", 104);
                result.put("Msg", "Object is In Checking");
                renderJson(result);
                return;
            } else {
                result.put("Result", a);
                a.remove("safequestion", "safeanswer", "enable", "password");
                a.put("provinceSTR", null);
                a.put("citySTR", null);
                try {
                    Record province = Db.findById("mb_provinces",
                            a.getInt("province"));
                    a.put("provinceSTR", province.getStr("values"));
                } catch (Exception e) {
                    result.put("Msg", "Can Not Get City And Province Info");
                }
                try {
                    Record city = Db.findById("mb_cities", a.getInt("city"));
                    a.put("citySTR", city.getStr("values"));
                } catch (Exception e) {
                    result.put("Msg", "Can Not Get City And Province Info");
                }
                if (a.get("corpname") == null)
                    a.remove("corpname", "corpaddress");
                renderJson(result);
                this.setSessionAttr("user", user);
                this.setSessionAttr("userID", a.get("id"));
                return;
            }
        }
    }
    public void logout() {
        this.removeSessionAttr("userID");
        this.removeSessionAttr("user");
        String agent = this.getRequest().getHeader("agent");
        if (this.getRequest().getHeader("token") == null) {
            renderJson(new Result());
            return;
        }
        Map<String, Object> result = authStatus();
        if (result == null) {
            renderJson(new Result().lackOfPara());
            return;
        }
        if (agent != null)
            result.put(agent, "offline");
        else
            result.put("WEB", "offline");
        // System.out.println(result);
        new JWTSigner("SuperMTNeverFallBack").sign(result);
        try {
            User.dao.findById(result.get("userid"))
                    .set("token",
                            new JWTSigner("SuperMTNeverFallBack").sign(result))
                    .update();
            this.getResponse().setHeader("token",
                    new JWTSigner("SuperMTNeverFallBack").sign(result));
            renderJson(new Result());
        } catch (Exception e) {
            renderJson(new Result().needAuth());
            return;
        }
        //
        //
    }
    private Map<String, Object> authStatus() {
        String token = this.getRequest().getHeader("token");
        String agent = this.getRequest().getHeader("agent");
        return authStatus(token, agent);
    }
    private Map<String, Object> authStatus(String token, String agent) {
        JWTVerifier verfier = new JWTVerifier("SuperMTNeverFallBack");
        Map<String, Object> result = null;
        if (token == null) {
            return null;
        }
        try {
            result = verfier.verify(token);
            if (agent == null) {
                agent = "WEB";
                result.put(agent,
                        System.currentTimeMillis() + 1000 * 60 * 60 * 24);
            }
            // renderJson(result);
        } catch (InvalidKeyException | NoSuchAlgorithmException
                | IllegalStateException | SignatureException | IOException
                | JWTVerifyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // renderJson(result);
        return result;
    }
    @Clear(UserAuth.class)
    public void forget() {
        Result result = new Result();
        if (!this.getRequest().getMethod().equals("POST")) {
            result.put("Code", 102);
            result.put("Msg", "Illegal Action Paramter");
        } else {
            String name = getPara("username");
            String safeQue = getPara("safeque");
            String safeAns = getPara("safeans");
            String newPass = getPara("newpass");
            if (name != null && safeQue != null && safeAns != null
                    && newPass != null) {
                User temp = User.dao.findFirst(
                        "Select * from user where username = ? and safequestion = ? and safeanswer = ?",
                        name, safeQue, safeAns);
                if (temp == null) {
                    result.put("Code", 104);
                    result.put("Msg", "Can not Find Object");
                    renderJson(result);
                    return;
                } else {
                    temp.set("password", newPass).update();
                }
            } else {
                result.put("Code", 105);
                result.put("Msg", "Lack Of Parameters");
            }
        }
        renderJson(result);
    }
    @Clear(UserAuth.class)
    public void signup() {
        Result result = new Result();
        if (!this.getRequest().getMethod().equals("POST")) {
            result.put("Code", 102);
            result.put("Msg", "Illegal Action Paramter");
        } else {
            String username = getPara("username");
            String password = getPara("password");
            String question = getPara("safeque");
            String answer = getPara("safeans");
            if (username != null && password != null && question != null
                    && answer != null) {
                if (!User.dao
                        .find("Select * from user where username = ?", username)
                        .isEmpty()) {
                    result.put("Code", 103);
                    result.put("Msg", "Repeating User Name");
                    renderJson(result);
                    return;
                }
                User temp = new User();
                temp.set("username", username).set("password", password)
                        .set("realname", getPara("realname"))
                        .set("phone", getPara("phone"))
                        .set("mail", getPara("mail"))
                        .set("province", getPara("province"))
                        .set("city", getPara("city"))
                        .set("safequestion", question).set("safeanswer", answer)
                        .set("type", 4)
                        .set("regtime",
                                new Timestamp(System.currentTimeMillis()))
                        .save();
                result.put("Result", temp);
            } else {
                result.put("Code", 105);
                result.put("Msg", "Lack Of Parameters");
            }
        }
        renderJson(result);
    }
    @Clear(PostMethod.class)
    // @Before(UserAuth.class)
    @ActionKey("/auth/upgrade/musician")
    public void upgradedmusician() {
        Result result = new Result();
        if (!this.getRequest().getMethod().equals("POST")) {
            result.clear();
            result.put("success", false);
            result.put("msg", "This ACTION Only Accept POST METHOD");
            result.put("file_path", null);
            renderJson(result);
            return;
        }
        // System.out.println(this.getRequest().getContentType());
        if (!this.getRequest().getContentType()
                .contains("multipart/form-data")) {
            result.clear();
            result.put("success", false);
            result.put("msg", "Illegal Action Method");
            result.put("file_path", null);
            renderJson(result);
            return;
        }
        Integer userID = getID();
        File photo = getFile("photo") == null ? null
                : getFile("photo").getFile();
        if (userID == 0) {
            result.clear();
            result.put("success", false);
            result.put("msg", "Need Auth");
            result.put("file_path", null);
            renderJson(result);
            return;
        }
        String nickname = getPara("nickname");
        String IDnumber = getPara("IDnumber");
        Musician temp = Musician.dao
                .findFirst("Select * from musician where userid = ?", userID);
        if (temp != null) {
            if (nickname != null)
                temp.set("nickname", nickname);
            if (IDnumber != null)
                temp.set("IDnumber", IDnumber);
            result.clear();
            result.put("file_path", null);
            if (photo != null) {
                String a = FileController.moveFile(String.valueOf(userID),
                        photo, "idphoto");
                temp.set("IDphotopath", a).update();
                result.put("file_path", a);
            }
            temp.set("addtime", new Timestamp(System.currentTimeMillis()))
                    .update();
            result.put("success", true);
            result.put("msg", "update success");
            renderJson(result);
            return;
        }
        if (photo == null) {
            result.put("success", false);
            result.put("msg", "Lack of photo file");
            result.put("file_path", null);
            renderJson(result);
            return;
        }
        if (nickname == null || IDnumber == null) {
            result.clear();
            result.put("success", false);
            result.put("msg", "Lack of Parameters");
            result.put("file_path", null);
            renderJson(result);
            return;
        }
        String a = FileController.moveFile(String.valueOf(userID), photo,
                "idphoto");
        temp = new Musician();
        temp.set("userid", userID).set("nickname", nickname).set("status", 1)
                .set("IDnumber", IDnumber).set("IDphotopath", a)
                .set("addtime", new Timestamp(System.currentTimeMillis()))
                .save();
        Photo ptemp = new Photo();
        ptemp.set("userid", userID).set("photopath", a)
                .set("addtime", new Timestamp(System.currentTimeMillis()))
                .save();
        result.clear();
        result.put("success", true);
        result.put("msg", "Seems So GOOD");
        result.put("file_path", a);
        renderJson(result);
        return;
    }
    @ActionKey("/auth/upgrade/org")
    public void upgrandorg() {
        Result result = new Result();
        Integer userID = getID();
        if (userID == 0) {
            result.needAuth();
            renderJson(result);
            return;
        }
        User temp = User.dao.findById(userID);
        if (temp == null) {
            result.needAuth();
            renderJson(result);
            return;
        }
        String corpAddr = getPara("corpAddr");
        String corpName = getPara("corpName");
        if (corpAddr == null || corpName == null) {
            result.lackOfPara();
            renderJson(result);
            return;
        }
        temp.set("corpaddress", corpAddr).set("corpname", corpName)
                .set("enable", 3).set("type", 3).update();
        renderJson(result);
        return;
    }
    // 无视他吧。。。
    @ActionKey("/auth/upgrade/vip")
    public void vip() {
        Result result = new Result();
        Integer userID = getID();
        if (userID == 0) {
            result.needAuth();
            renderJson(result);
            return;
        }
        String durationStr = getPara("duration");
        Long duration = Long.valueOf(durationStr);
        String unit = getPara("unit");
        Timestamp starttime = new Timestamp(System.currentTimeMillis());
        duration = duration * 60 * 60;
        System.out.println(duration);
        // endtime.setd
        switch (unit) {
            case "year":
                duration *= 12;
            case "month":
                duration *= 31;
            case "day":
                duration *= 24;
                break;
            default: {
                result.put("Code", 105);
                result.put("Msg", "Error Parameter —— unit");
                renderJson(result);
                return;
            }
        }
        System.out.println(duration);
        Timestamp endtime = new Timestamp(
                System.currentTimeMillis() + duration * 1000);
        if (!payAuthority(duration)) {
            result.put("Code", 101);
            result.put("Msg", "Show me the MONEY");
            renderJson(result);
            return;
        }
        // VIP temp = new VIP();
        // temp.set("userid", userID).set("starttime", "").save();
        result.put("Result", endtime);
        renderJson(result);
    }
    private boolean payAuthority(Long duration) {
        return true;
    }
    public void me() {
        Result result = new Result();
        switch (this.getRequest().getMethod()) {
            case "GET": {
                Integer userID = getID();
                if (userID == 0) {
                    result.needAuth();
                    renderJson(result);
                    return;
                }
                System.out.println(userID);
                User a = User.dao.findById(userID);
                a.remove("safequestion", "safeanswer", "password");
                a.put("provinceSTR", null);
                a.put("citySTR", null);
                try {
                    if (a.get("corpname") == null)
                        a.remove("corpname", "corpaddress");
                    Record province = Db.findById("mb_provinces",
                            a.getInt("province"));
                    a.put("provinceSTR", province.getStr("values"));
                } catch (NullPointerException e) {
                    result.put("Msg", "None Province");
                }
                try {
                    Record city = Db.findById("mb_cities", a.getInt("city"));
                    a.put("citySTR", city.getStr("values"));
                    result.put("Result", a);
                } catch (NullPointerException e) {
                    result.put("Msg", "None City");
                    result.put("Result", a);
                }
                renderJson(result);
                return;
            }
            case "POST": {
                Integer userid = getID();
                if (userid == 0) {
                    result.put("Code", 101);
                    result.put("Msg", "Need Auth");
                    renderJson(result);
                    return;
                }
                User temp = User.dao.findById(userid);
                if (temp == null) {
                    result.put("Code", 104);
                    result.put("Msg", "Can not find Object");
                    renderJson(result);
                    return;
                }
                String proNum = getPara("province");
                String citNum = getPara("city");
                String que = getPara("safeque");
                String ans = getPara("safeans");
                String realname = getPara("realname");
                String phone = getPara("phone");
                String mail = getPara("mail");
                if (que != null)
                    temp.set("safequestion", que);
                if (ans != null)
                    temp.set("safeanswer", ans);
                if (realname != null)
                    temp.set("realname", realname);
                if (phone != null)
                    temp.set("phone", phone);
                if (mail != null)
                    temp.set("mail", mail);
                if (proNum != null)
                    temp.set("province", proNum);
                if (citNum != null)
                    temp.set("city", citNum);
                temp.update();
                temp.put("provinceSTR", null);
                temp.put("citySTR", null);
                try {
                    Record province = Db.findById("mb_provinces",
                            temp.getInt("province"));
                    temp.put("provinceSTR", province.getStr("values"));
                } catch (Exception e) {
                    result.put("Msg", "Can Not Get City And Province Info");
                }
                try {
                    Record city = Db.findById("mb_cities", temp.getInt("city"));
                    temp.put("citySTR", city.getStr("values"));
                } catch (Exception e) {
                    result.put("Msg", "Can Not Get City And Province Info");
                }
                result.put("Result", temp);
                renderJson(result);
                break;
            }
            default: {
                result.put("Code", 102);
                result.put("Msg", "Illegal Acrtion Method");
            }
        }
        renderJson(result);
    }
    @ActionKey("/me/music")
    public void myMusic() {
        Result result = new Result();
        if (!this.getRequest().getMethod().equals("GET")) {
            result.illegalMethod();
            renderJson(result);
            return;
        }
        try {
            Musician current = Musician.dao.findFirst(
                    "Select * from musician where userid = ?", getID());
            String pageNum = getPara("pageNum") == null ? "1"
                    : getPara("pageNum");
            String pageSize = getPara("pageSize") == null ? "20"
                    : getPara("pageSize");
            Page<Song> songlist = Song.dao.paginate(Integer.valueOf(pageNum),
                    Integer.valueOf(pageSize), "Select * ",
                    "from song where musicianid = ? ORDER BY addtime DESC",
                    (Integer) current.get("id"));
            result.put("Result", songlist);
        } catch (Exception e) {
            e.printStackTrace();
            result.needAuth();
            result.put("Msg", "You are not Musician");
        }
        renderJson(result);
    }
    private Integer getID() {
        return Integer.valueOf(this.getRequest().getAttribute("id").toString());
    }
    public void getprovince() {
        Result result = new Result();
        List<Record> provinces = Db.find("select * from mb_provinces");
        result.put("Provinces", provinces);
        renderJson(result);
    }
    public void getcity() {
        Result result = new Result();
        String proid = getPara("provinceid");
        String pageNum = getPara("pageNum") == null ? "1" : getPara("pageNum");
        String pageSize = getPara("pageSize") == null ? "20"
                : getPara("pageSize");
        if (proid != null) {
            result.put("Code", 200);
            Page<Record> cities = Db.paginate(Integer.valueOf(pageNum),
                    Integer.valueOf(pageSize), "select *",
                    "from mb_cities where provinceid = ?", proid);
            result.put("Cities", cities);
        } else {
            result.put("Code", 105);
            result.put("Msg", "Lack of provinceid");
        }
        renderJson(result);
    }
    /*
     * @Before(UserAuth.class) public void modify(){ Result result = new Result(); String id =
     * getPara("id"); String que = getPara("safeque"); String ans = getPara("safeans"); if (que ==
     * null || ans ==null){ result.put("Code", 105); result.put("Msg", "Lack Of Parameters");
     * renderJson(result); return ; } User temp = User.dao.findById(id); if (temp==null){
     * result.put("Code", 104); result.put("Msg", "Can not find Object"); renderJson(result); return
     * ; } if (!temp.get("safequestion").equals(que)||!temp.get("safeanswer").equals(ans)){
     * result.put("Code", 103); result.put("Msg", "Wrong Question Or Answer"); renderJson(result);
     * return ; } String realname = getPara("realname"); String phone = getPara("phone"); String
     * mail = getPara("mail"); if (realname!=null) temp.set("realname", realname); if (phone!=null)
     * temp.set("phone", phone); if (mail!=null) temp.set("mail", mail); temp.update();
     * result.put("Result",temp); renderJson(result); }
     */
    @Clear(PostMethod.class)
    public void avatar() {
        Result result = new Result();
        File photo = getFile("photo") == null ? null
                : getFile("photo").getFile();
        if (photo == null) {
            result.clear();
            result.put("success", false);
            result.put("msg", "Lack of Parameters");
            result.put("file_path", null);
            renderJson(result);
            return;
        }
        Integer userID = getID();
        if (userID == 0) {
            result.put("Code", 101);
            result.put("Msg", "Need Auth");
            renderJson(result);
            return;
        }
        String a = FileController.moveFile(String.valueOf(userID), photo,
                "avatar");
        User.dao.findById(userID).set("avatar", a).update();
        result.clear();
        result.put("success", true);
        result.put("msg", "Seems So GOOD");
        result.put("file_path", a);
        renderJson(result);
        return;
    }
    @ActionKey("/musician")
    public void musician() {
        Result result = new Result();
        switch (this.getRequest().getMethod()) {
            case "GET": {
                String a = getPara("id");
                if (a != null) {
                    Musician temp = Musician.dao.findFirst(
                            "Select * From musician where id = ? and status = 3",
                            a);
                    // List<TopicReply> replyList = TopicReply.dao.find("Select * from topicreply
                    // where topicid = ?",a);
                    if (temp != null) {
                        temp.set("views", (temp.getInt("views") == null ? 0
                                : temp.getInt("views")) + 1).update();
                        temp.remove("IDnumber").remove("IDphotopath");
                        result.put("Code", 100);
                        result.put("Result", temp);
                    } else if (Musician.dao.findFirst(
                            "Select * From musician where id = ? and status != 3",
                            a) != null) {
                        result.put("Code", 104);
                        result.put("Msg", "Object has been DISABLED");
                    } else {
                        result.put("Code", 104);
                        result.put("Msg", "Can Not Find Object");
                    }
                } else {
                    String mname = getPara("name");
                    if (mname != null) {
                        String pageNum = getPara("pageNum") == null ? "1"
                                : getPara("pageNum");
                        String pageSize = getPara("pageSize") == null ? "20"
                                : getPara("pageSize");
                        Page<Musician> musicianList = Musician.dao
                                .paginate(Integer.valueOf(pageNum),
                                        Integer.valueOf(pageSize), "select *",
                                        "from musician where status = 3 and nickname LIKE '%"
                                                + mname
                                                + "%' order by id DESC");
                        List<Musician> templist = musicianList.getList();
                        Musician temp;
                        for (int i = 0; i < templist.size(); i++) {
                            temp = templist.get(i);
                            temp.set("views",
                                    (temp.getInt("views") == null ? 0
                                            : temp.getInt("views")) + 1)
                                    .update();
                        }
                        result.put("Code", 200);
                        result.put("Result", musicianList);
                    } else {
                        String pageNum = getPara("pageNum") == null ? "1"
                                : getPara("pageNum");
                        String pageSize = getPara("pageSize") == null ? "20"
                                : getPara("pageSize");
                        Page<Musician> musicianList = Musician.dao.paginate(
                                Integer.valueOf(pageNum),
                                Integer.valueOf(pageSize), "select *",
                                "from musician where status = 3 ORDER BY views DESC");
                        result.put("Code", 200);
                        result.put("Result", musicianList);
                        // 来点劲爆的
                        for (Musician temp : musicianList.getList()) {
                            temp.put("songs", null);
                            List<Song> songList = Song.dao.find(
                                    "Select * from song where musicianid = ? order by views DESC limit 3",
                                    temp.getInt("id"));
                            temp.put("songs", songList);
                            temp.remove("IDnumber").remove("IDphotopath");
                        }
                    }
                }
                break;
            }
            case "POST": {
                String a = getPara("id");
                Integer b = getID();
                if (a != null) {
                    if (!a.equals(b.toString())) {
                        result.put("Code", 101);
                        result.put("Msg", "Need Auth");
                        renderJson(result);
                        return;
                    }
                    Musician temp = Musician.dao.findFirst(
                            "Select * From musician where id = ? and status = 3",
                            a);
                    // List<TopicReply> replyList = TopicReply.dao.find("Select * from topicreply
                    // where topicid = ?",a);
                    if (temp != null) {
                        String nickname = getPara("nickname") == null ? null
                                : getPara("nickname");
                        if (nickname != null)
                            temp.set("nickname", nickname).update();
                        result.put("Code", 100);
                        result.put("Result", temp);
                    } else if (Musician.dao.findFirst(
                            "Select * From musician where id = ? and status != 3",
                            a) != null) {
                        result.put("Code", 104);
                        result.put("Msg", "Object has been DISABLED");
                    } else {
                        result.put("Code", 104);
                        result.put("Msg", "Can Not Find Object");
                    }
                } else {
                    result.put("Code", 105);
                    result.put("Msg", "Lack Of musicianID");
                }
                break;
            }
            default: {
                result.put("Code", 102);
                result.put("Msg", "Illegal Action Parameters");
            }
        }
        renderJson(result);
        return;
    }
    public void careme() {
        Result result = new Result();
        Integer id = getID();// 用户id
        if (id == 0) {
            result.needAuth();
            renderJson(result);
            return;
        }
        String type = getPara("usertype");// 用户类型
        String pageNum = getPara("pageNum") == null ? "1" : getPara("pageNum");
        String pageSize = getPara("pageSize") == null ? "20"
                : getPara("pageSize");
        if (type == null) {
            result.lackOfPara();
            renderJson(result);
            return;
        }
        if (this.getRequest().getMethod().equals("GET")) {
            switch (type) {
                case "user": {
                    Page<Record> pairlist = Db.paginate(
                            Integer.valueOf(pageNum), Integer.valueOf(pageSize),
                            "SELECT *",
                            "from relation_follow where userid2 = ? and enable = 1",
                            id);
                    String targetid;
                    for (Record a : pairlist.getList()) {
                        a.remove("userid");
                        targetid = a.getInt("userid2").toString();
                        a.remove("userid2");
                        a.set("name",
                                User.dao.findById(targetid).getStr("username"));
                    }
                    result.put("Code", 200);
                    result.put("Result", pairlist);
                    result.put("Code", 200);
                    result.put("Result", pairlist);
                    break;
                }
                case "musician": {
                    Page<Record> pairlist;
                    try {
                        pairlist = Db.paginate(Integer.valueOf(pageNum),
                                Integer.valueOf(pageSize), "SELECT *",
                                "from relation_fans where musicianid = ? and enable = 1",
                                Musician.dao.findById(id).getInt("id")
                                        .toString());
                    } catch (NullPointerException e) {
                        result.needAuth();
                        result.put("Msg", "user is not an musician");
                        return;
                    }
                    String targetid;
                    for (Record a : pairlist.getList()) {
                        a.remove("userid");
                        targetid = a.getInt("musicianid").toString();
                        a.remove("musicianid");
                        a.set("name", Musician.dao.findById(targetid)
                                .getStr("nickname"));
                    }
                    result.put("Code", 200);
                    result.put("Result", pairlist);
                    break;
                }
                default: {
                    result.put("Code", 105);
                    result.put("Msg", "Illegal Parameters");
                }
            }
        } else {
            result.illegalMethod();
        }
        renderJson(result);
    }
    private void encodeToken() {
        JWTSigner a = new JWTSigner("123443212341234124124");
        Map claims = new HashMap();
        claims.put("123", "321");
        System.out.println(a.sign(claims));
        renderText("succ");
    }
    private boolean validateAuthToken() {
        JWTVerifier b = new JWTVerifier("123443212341234124124");
        try {
            Map result = b.verify(
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyIxMjMiOiIzMjEifQ.XrAdJyJ_VKmsk2qYpZGmIzoOpv18ggKTaTvLcqhcJxc");
            renderJson(result);
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JWTVerifyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    @Clear
    @ActionKey("/finduser")
    public void findUser() {
        Result result = new Result();
        if ("GET".equals(this.getRequest().getMethod())) {
            String uid = getPara("id");
            if (uid == null) {
                result.lackOfPara();
                renderJson(result);
                return;
            }
            User temp = User.dao.findById(uid);
            if (temp != null) {
                Map bak = new HashMap();
                bak.put("username", temp.get("username"));
                bak.put("avatar", temp.get("avatar"));
                result.put("Result", bak);
            } else {
                result.nullObject();
            }
        } else {
            result.illegalMethod();
        }
        renderJson(result);
    }
    @ActionKey("/ping/redis")
    public void pingredis() {
        renderText(CacheIniter.jedis.ping());
    }
    // @Before(UserAuth.class)
    public void test() {
        renderText(this.getRequest().getAttribute("token").toString());
    }
    @Clear
    @ActionKey("/auth/gitGetCode")
    public void git_oauth_getCode() throws IOException {
        String responseBody = sendCode(this.getRequest().getParameter("code"));
        String userInfo = sendToken(responseBody);
        if (userInfo == null) {
            renderJson(new Result().nullObject());
            return;
        }
        JSONObject userMap = JSON.parseObject(userInfo);
        String usermail = userMap.getString("email");// 只有邮箱可以唯一限定用户。。。用户不能
        if (usermail == null) {
            renderJson(new Result().needAuth().put("Msg", "No Email Info"));// 放弃治疗，没有邮箱就算了吧
            return;
        }
        String avatar = userMap.getString("avatar_url");
        if (User.mailexists(usermail)) {
            // 有这个用户，登录
            User user = User.dao
                    .find("select * from user where mail = ?", usermail).get(0);
            this.login(user.getStr("username"), user.getStr("password"));
            this.redirect301(MainConfig.OAuthRedirect);
            return;
        } else {
            // 没有这个用户，为其创建一个用户后绑定，登录
            Record user = User.createUser(usermail, usermail).set("mail",
                    usermail);
            Db.update("user", user);
            this.login(user.getStr("username"), user.getStr("password"));
            this.redirect301(MainConfig.OAuthRedirect);
            return;
        }
    }
    private String sendCode(String code) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType
                .parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType,
                "client_id=" + MainConfig.GIT_CLIENT_ID + "&client_secret="
                        + MainConfig.GIT_CLIENT_SECRET + "&code=" + code);
        Request request = new Request.Builder()
                .url("https://github.com/login/oauth/access_token").post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache").build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    private String sendToken(String access_token) throws IOException {
        if (access_token == null)
            return null;
        if (!access_token.contains("access_token")) {
            return null;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.github.com/user").get()
                .addHeader("authorization",
                        "token " + access_token.substring(
                                access_token.indexOf('=') + 1,
                                access_token.indexOf('&')))
                .addHeader("cache-control", "no-cache").build();
        Response response = client.newCall(request).execute();
        //
        // System.out.println(access_token.substring(access_token.indexOf('=')+1,
        // access_token.indexOf('&')));;
        return response.body().string();
    }
    @Clear
    @ActionKey("/auth/baiduGetCode")
    public void baidu_oauth_getCode() throws IOException {
        String responseCode = this.getRequest().getParameter("code");
        // renderJson(responseBody);
        // this.login("supermt", "sasomi");
        // this.redirect301(MainConfig.OAuthRedirect);
        String accessToken = getBaiduAccessToken(responseCode);
        if (accessToken == null) {
            renderJson(new Result().nullObject());
            return;
        } else {
            String username = getBaiduUserName(accessToken);
            if (User.nameexists(username)) {
                User user = User.dao
                        .find("select * from user where username = ?", username)
                        .get(0);
                this.login(user.getStr("username"), user.getStr("password"));
                System.out.println("User Existed");
                this.redirect301(MainConfig.OAuthRedirect);
                return ;
            } else {
                Record user = User.createUser(username, username);
                Db.update("user", user);
                this.login(user.getStr("username"), user.getStr("password"));
                System.out.println("User Created");
                this.redirect301(MainConfig.OAuthRedirect);
                return ;
            }
        }
    }
    private String getBaiduUserName(String token) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{\n\t\"username\":\"supermt\",\n\t\"password\":\"sasomi\"\n}");
        Request request = new Request.Builder()
                .url("https://openapi.baidu.com/rest/2.0/passport/users/getInfo?access_token="
                        + token)
                .get().addHeader("content-type", "application/json")
                .addHeader("authorization",
                        "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzdXBlcm10MiIsImNyZWF0ZWQiOjE0OTQ3NDcyODQ2MzQsImV4cCI6MTQ5NTM1MjA4NH0.Ole7W8MvAG1n9Pg2P_wMZoJ3fLEswDJ-NcySawLQaqi1H6MuLsnON6OG26qfgaetxhj7_IUy2nadSnFow3_t0w")
                .addHeader("cache-control", "no-cache").build();
        Response response = client.newCall(request).execute();
        JSONObject jsonObject = JSON.parseObject(response.body().string());
        return jsonObject.getString("username");
    }
    private String getBaiduAccessToken(String code) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "{\n\t\"username\":\"supermt\",\n\t\"password\":\"sasomi\"\n}");
        Request request = new Request.Builder()
                .url("https://openapi.baidu.com/oauth/2.0/token?grant_type=authorization_code&code="
                        + code
                        + "&client_id=bb5m9Pz07QzIQWkHGp1HWQcd&client_secret=OghMlrrirgM1xr9xtnDIfpjMpQQhGbmR&redirect_uri=http%3A%2F%2Fimusic.jotang.party%2Fapi%2Fauth%2FbaiduGetCode")
                .get().addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache").build();
        Response response = client.newCall(request).execute();
        JSONObject jsonObject = JSON.parseObject(response.body().string());
        return jsonObject.getString("access_token");
    }
    @Clear
    public String baiduSendCode(String code) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType
                .parse("application/x-www-form-urlencoded");
        Request request = new Request.Builder()
                .url("https://openapi.baidu.com/oauth/2.0/token?grant_type=authorization_code&code="
                        + code + "&client_id=" + "8l9QNm9PzFUik1jZf18TEwqa"
                        + "&client_secret=s1AgOmcQyYbKXeieGeklHKhtHqmHAcT8&"
                        + "redirect_uri=http://supermt.ittun.com/api/auth/baiduGetCode")
                .get()
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache").build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
