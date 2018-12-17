package imusic.server.Controller;

import java.io.File;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.upload.UploadFile;

import imusic.server.aop.LogProducer;
import imusic.server.aop.PostMethod;
import imusic.server.aop.UserAuth;
import imusic.server.models.User;
@Before(UserAuth.class)
@Clear(PostMethod.class)
public class FileController extends Controller {
	
	//@ActionKey("/api/file/upload")
	private Integer getID(){
//		String token = this.getRequest().getHeader("token");
//		String userid = this.getSessionAttr("userID")==null ? null : this.getSessionAttr("userID").toString();
//		//System.out.println(userid);
//		if (userid == null && token!=null){
//			User temp = User.dao.findFirst("Select * from user where token = ?",token);
//			if (temp!=null)
//			return Integer.valueOf( temp.getInt("id"));
//			else return 0;
//		}
//		else if (userid == null) return 0;
//		else return Integer.valueOf(userid);
		return Integer.valueOf(this.getRequest().getAttribute("id").toString());
	}
	public String upload(){
		Result result = new Result();
		result.clear();
		if (this.getRequest().getMethod().equals("POST")){
			File temp = getFile("filedata").getFile();
			String type = getPara("type");
			if (!type.equals("photo")&&!type.equals("music")){
				result.put("success", false);
				result.put("msg", "Error Parametetr —— type");
				result.put("file_path",null);
				renderJson(result);
			}
			if (type == null || temp == null){
				result.put("success", false);
				result.put("msg", "Lack of Parameters");
				result.put("file_path",null);
				renderJson(result);
				return null;
			}
			Integer uid = getID();
			if (uid == null || uid==0){
				result.put("success", false);
				result.put("msg", "Need Auth");
				result.put("file_path",null);
				renderJson(result);
				return null;
			}
			String a = FileController.moveFile(String.valueOf(uid),temp,type);
			result.put("success", true);
			result.put("msg", "Seems So GOOD");
			result.put("file_path", a);
			renderJson(result);
			return a;
		}
		else {
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
			renderJson(result);
			return null;
		}
	}
	@Clear
	@ActionKey("/system/upload")
	@Before(LogProducer.class)
	public String adminUpload(){
		Result result = new Result();
		result.clear();
		if (this.getRequest().getMethod().equals("POST")){
			File temp = getFile("filedata").getFile();
			String type = getPara("type");
			if (type == null || temp == null){
				result.put("success", false);
				result.put("msg", "Lack of Parameters");
				result.put("file_path",null);
				renderJson(result);
				return null;
			}
			Integer uid = this.getSessionAttr("adminID");
			if (uid == null || uid.equals(0)){
				result.put("success", false);
				result.put("msg", "Need Auth");
				result.put("file_path",null);
				renderJson(result);
				return null;
			}
			String a = FileController.moveFile(String.valueOf(uid),temp,"admin/"+type);
			result.put("success", true);
			result.put("msg", "Seems So GOOD");
			result.put("file_path", a);
			renderJson(result);
			return a;
		}
		else {
			result.put("Code", 102);
			result.put("Msg", "Illegal Action Parameters");
			renderJson(result);
			return null;
		}
	}
	
	@Clear()
	public void download(){
		Result result = new Result();
		String path = getPara("path");
		if (path == null){ 
			result.put("Code", 105);
			result.put("Msg", "Lack of FilePath");
			renderJson(result);
			return ;
		}
		System.out.println(PathKit.getWebRootPath()+path);
		File down = new File(PathKit.getWebRootPath()+path);
		if (down == null || !down.exists()){
			result.put("Code", 104);
			result.put("Msg", "No Such File");
			renderJson(result);
			return ;
		}
		renderFile(down);
	}
	@Clear
	public static String moveFile(String uid,File temp,String type){
		String dest = PathKit.getWebRootPath()+"/file/"+type+"/"+uid+"/";
		File destDir = new File(dest);
		if (!destDir.exists())  
	        destDir.mkdirs();  
		System.out.println(dest);
		File target = new File(dest+temp.getName());
		if (target.exists()){
			target.delete();
		}
		temp.renameTo(new File(dest+temp.getName()));
		return "/file/"+type+"/"+uid+"/"+temp.getName();
	}
	@Clear
	public static File getFileFromPath(String filePath){
		File temp = new File(filePath);
		return temp.exists() ? temp : null;
	}
}
