package oa.web.controller.common;

import com.common.dict.Constant2;
import com.common.util.ImageHWUtil;
import com.common.util.SystemHWUtil;
import com.common.util.WebServletUtil;
import com.io.hw.file.util.FileUtils;
import com.string.widget.util.ValueWidget;
import com.time.util.TimeHWUtil;
import net.sf.json.JSONObject;
import oa.bean.UploadResult;
import oa.dao.common.CompressFailedPicDao;
import oa.entity.common.AccessLog;
import oa.entity.common.CompressFailedPic;
import oa.service.DictionaryParam;
import oa.util.HWUtils;
import oa.web.controller.base.BaseController;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
//@Scope(value = "prototype")
@RequestMapping("/upload")
public class UploadController extends BaseController {
    public static final int UPLOAD_RESULT_SUCCESS = 0;
    public static final int UPLOAD_RESULT_FAILED = 1;
    private static HashMap<String, String> extMap;
    private static long maxSize;

    static {
        extMap = new HashMap<String, String>();
        extMap.put("image", "gif,jpg,jpeg,png,bmp,GIF,JPG,JPEG,PNG,BMP");
        extMap.put("flash", "swf,flv,SWF,FLV");
        extMap.put(
                "media",
                "swf,flv,mp3,wav,wma,wmv,mid,avi,mpg,asf,rm,rmvb,SWF,FLV,MP3,WAV,WMA,WMV,MID,AVI,MPG,ASF,RM,RMVB");
        extMap.put(
                "file",
                "doc,docx,xls,xlsx,ppt,htm,html,txt,zip,rar,gz,bz2,DOC,DOCX,XLS,XLSX,PPT,HTM,HTML,TXT,ZIP,RAR,GZ,BZ2");

    }

    private CompressFailedPicDao compressFailedPicDao;

    private static void init() {
        // 最大文件大小
        String maxSizeStr = DictionaryParam.get(Constant2.DICTIONARY_GROUP_GLOBAL_SETTING, "pic_max_size");
        maxSize = Constant2.UPLOAD_SIZE_DEFAULT;
        if (!ValueWidget.isNullOrEmpty(maxSizeStr)) {//若没有设置pic_max_size,则采用默认值(Constant2.UPLOAD_SIZE_DEFAULT)
            maxSize = Long.parseLong(maxSizeStr);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/init", produces = SystemHWUtil.RESPONSE_CONTENTTYPE_JSON_UTF)
    public String init(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        init();
        return Constant2.RESPONSE_RIGHT_RESULT;
    }

    @ResponseBody
    @RequestMapping(value = "/upload_video", produces = SystemHWUtil.RESPONSE_CONTENTTYPE_JSON_UTF)
    public String uploadVideo(
            @RequestParam(value = "video_path", required = false) MultipartFile file, String uploadFolder, String needMD5,
            HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String content = null;
        Map map = new HashMap();
        if (ValueWidget.isNullOrEmpty(file)) {
            map.put("error", "not specify file!!!");
        } else {
            System.out.println("request:" + request);// org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest@7063d827
            System.out.println("request:" + request.getClass().getSuperclass());
            // String formFileTagName=null;//"file2"
            // for( ;multi.hasMoreElements();){
            // String element=multi.nextElement();
            // formFileTagName=element;//表单中标签的名称：file标签的名称
            // // System.out.println("a:"+element+":$$");
            // break;
            // }
            String fileName = file.getOriginalFilename();// 上传的文件名
            fileName = fileName.replaceAll("[\\s]", "");//IE中识别不了有空格的json
            // 保存到哪儿
            String finalFileName = TimeHWUtil.formatDateByPattern(TimeHWUtil
                    .getCurrentTimestamp(), "yyyyMMddHHmmss") + "_"
                    + new Random().nextInt(1000) + fileName;
            String relativePath = null;
            if (ValueWidget.isNullOrEmpty(uploadFolder)) {
                relativePath = Constant2.UPLOAD_FOLDER_NAME + "/image";
            } else {
                relativePath = uploadFolder;
            }

//			File savedFile = WebServletUtil.getUploadedFilePath(request,relativePath
//					, finalFileName,
//					Constant2.SRC_MAIN_WEBAPP);// "D:\\software\\eclipse\\workspace2\\demo_channel_terminal\\ upload\\pic\\ys4-1.jpg"
            File savedFile = new File(DictionaryParam.get("global_setting", "uploadVideoPrefix"), finalFileName);
            File parentFolder = SystemHWUtil.createParentFolder(savedFile);
            FileUtils.makeWritable(parentFolder);//使...可写
            System.out.println("[upload]savedFile:"
                    + savedFile.getAbsolutePath());
            // 保存
            try {
                file.transferTo(savedFile);
                if (FileUtils.isImageFile(fileName)) {
                    ImageHWUtil.compressImg(savedFile, null, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // FileUtils.moveFile(uploadFile, savedFile);
            // if(!savedFile.exists()){
            // SystemHWUtil.copyFile(uploadFile, savedFile/*new
            // File("D:\\Temp\\a\\a\\c.zip")*/);
            // uploadFile.delete();
            // }
            // }
            ObjectMapper mapper = new ObjectMapper();
            String url2 = HWUtils.getRelativeUrl(request, relativePath, finalFileName);
            String fullUrl = null;//http://localhost:8080/tv_mobile/upload/image/20150329170823_2122015-03-23_01-42-03.jpg
            /***
             * request.getRequestURL():http://localhost:8081/SSLServer/addUser.security<br>
             * request.getServletPath():/addUser.security<br>
             * prefixPath:http://localhost:8080/tv_mobile/
             */
            fullUrl = HWUtils.getFullUrl(request, relativePath, finalFileName);
            if (!ValueWidget.isNullOrEmpty(needMD5) && needMD5.equalsIgnoreCase("need")) {
                String md5 = SystemHWUtil.getFileMD5(savedFile);
                map.put("md5", md5);
                map.put("MD5", md5);
            }
            map.put("fileName", finalFileName);
            map.put("remoteAbsolutePath", savedFile.getAbsolutePath());
            map.put("url", url2);
            map.put("fullUrl", fullUrl);
            map.put("relativePath", relativePath);
            session.setAttribute("finalFileName", finalFileName);
            try {
                content = mapper.writeValueAsString(map);
                System.out.println(content);
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//			System.out.println("map:"+map);
        }
//		ModelAndView modelAndView = new ModelAndView(
//				new MappingJacksonJsonView(), map);
/*
 * {"fileName":"20141002125209_571slide4.jpg","path":"D:\\software\\eclipse\\workspace2\\demo_channel_terminal\\upload\\image\\20141002125209_571slide4.jpg"}
 * */
//		response.addHeader("Access-Control-Allow-Origin", "*");
        return content;

    }


    /***
     * {"fileName":"20141002125209_571slide4.jpg","path":"D:\\software\\eclipse\\workspace2\\demo_channel_terminal\\upload\\image\\20141002125209_571slide4.jpg"}<br>
     * 改进如下:<br>
     * {
     * "path": "D:\\software\\eclipse\\workspace2\\tv_mobile\\target\\m2e-wtp\\web-resources\\upload\\image\\20150329165958_7952015-03-18_22-42-56.jpg",
     * "fileName": "20150329165958_7952015-03-18_22-42-56.jpg",
     * "url": "/tv_mobile/upload/image/20150329165958_7952015-03-18_22-42-56.jpg"
     * }
     *
     * @param file
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @ResponseBody
    @RequestMapping(value = "/upload", produces = SystemHWUtil.RESPONSE_CONTENTTYPE_JSON_UTF)
    public String upload(
            @RequestParam(value = "image223", required = false) MultipartFile file, String uploadFolder, String needMD5,
            HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String content = null;
        Map map = new HashMap();
        if (ValueWidget.isNullOrEmpty(file)) {
            map.put("error", "not specify file!!!");
        } else {
            System.out.println("request:" + request);// org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest@7063d827
            System.out.println("request:" + request.getClass().getSuperclass());
            // String formFileTagName=null;//"file2"
            // for( ;multi.hasMoreElements();){
            // String element=multi.nextElement();
            // formFileTagName=element;//表单中标签的名称：file标签的名称
            // // System.out.println("a:"+element+":$$");
            // break;
            // }
            AccessLog accessLog = logUploadFile(request);//记录日志
            String errorPrefix = "upload failed,error:";
            String fileName = file.getOriginalFilename();// 上传的文件名
            if (file.getSize() == 0) {
                String errorMessage = errorPrefix + "file size is zero";
                if (!ValueWidget.isNullOrEmpty(accessLog)) {
                    accessLog.setOperateResult(errorMessage);
                    logSave(accessLog, request);
                }
                return errorMessage;
            }

            UploadResult uploadResult = HWUtils.getSavedToFile(request, fileName, uploadFolder);
            File savedFile = uploadResult.getSavedFile();
            File parentFolder = SystemHWUtil.createParentFolder(savedFile);
            FileUtils.makeWritable(parentFolder);//使...可写
            System.out.println("[upload]savedFile:"
                    + savedFile.getAbsolutePath());

            // 保存
            try {
                file.transferTo(savedFile);
                accessLog.setOperateResult("upload success,file size:" + savedFile.length());
                if (FileUtils.isImageFile(fileName)) {
                    ImageHWUtil.compressImg(savedFile, null, null);
                }
            } catch (Exception e) {
                if (!ValueWidget.isNullOrEmpty(accessLog)) {
                    accessLog.setOperateResult("errorPrefix" + e.getMessage());
                    logSave(accessLog, request);
                }
                e.printStackTrace();
            }
            // FileUtils.moveFile(uploadFile, savedFile);
            // if(!savedFile.exists()){
            // SystemHWUtil.copyFile(uploadFile, savedFile/*new
            // File("D:\\Temp\\a\\a\\c.zip")*/);
            // uploadFile.delete();
            // }
            // }
            ObjectMapper mapper = new ObjectMapper();

            String rootPath = request.getContextPath();
            if (!rootPath.endsWith("/")) {
                rootPath = rootPath + "/";
            }
            String relativePath = uploadResult.getRelativePath();
            if (!relativePath.endsWith("/")) {
                relativePath = relativePath + "/";
            }
//            relativePath = relativePath + uploadResult.getFinalFileName();//upload/image/20150329170823_2122015-03-23_01-42-03.jpg
            String url2 = rootPath + relativePath;

            String fullUrl = null;//http://localhost:8080/tv_mobile/upload/image/20150329170823_2122015-03-23_01-42-03.jpg
            /***
             * request.getRequestURL():http://localhost:8081/SSLServer/addUser.security<br>
             * request.getServletPath():/addUser.security<br>
             * prefixPath:http://localhost:8080/tv_mobile/
             */
            fullUrl = HWUtils.getFullUrl(request, relativePath, uploadResult.getFinalFileName());
            if (!ValueWidget.isNullOrEmpty(needMD5) && needMD5.equalsIgnoreCase("need")) {
                String md5 = SystemHWUtil.getFileMD5(savedFile);
                map.put("md5", md5);
                map.put("MD5", md5);
            }
            map.put("fileName", uploadResult.getFinalFileName());
            map.put("remoteAbsolutePath", savedFile.getAbsolutePath());
            map.put("url", url2);
            map.put("fullUrl", fullUrl);
            map.put("relativePath", relativePath);
            session.setAttribute("finalFileName", uploadResult.getFinalFileName());
            try {
                content = mapper.writeValueAsString(map);
                System.out.println(content);
            } catch (JsonGenerationException e) {
                accessLog.setOperateResult(errorPrefix + e.getMessage());
                logSave(accessLog, request);
                e.printStackTrace();
            } catch (JsonMappingException e) {
                accessLog.setOperateResult(errorPrefix + e.getMessage());
                logSave(accessLog, request);
                e.printStackTrace();
            } catch (IOException e) {
                accessLog.setOperateResult(errorPrefix + e.getMessage());
                logSave(accessLog, request);
                e.printStackTrace();
            }
            accessLog.setDescription("uploaded file path:" + savedFile.getAbsolutePath());
            logSave(accessLog, request);
//			System.out.println("map:"+map);
        }
//		ModelAndView modelAndView = new ModelAndView(
//				new MappingJacksonJsonView(), map);
/*
 * {"fileName":"20141002125209_571slide4.jpg","path":"D:\\software\\eclipse\\workspace2\\demo_channel_terminal\\upload\\image\\20141002125209_571slide4.jpg"}
 * */
        return content;

    }

    /***
     * @param request
     * @param includeContextPath :  是否包含项目名
     * @return
     */
    private UploadResult beforeUpload(HttpServletRequest request, boolean includeContextPath) {
        // 文件保存目录路径
        String savePath = WebServletUtil.getUploadedFilePath(request,
                Constant2.UPLOAD_FOLDER_NAME + File.separator, Constant2.SRC_MAIN_WEBAPP
        ); // D:\software\eclipse\workspace2\demo_channel_terminal\src\main\webapp\
        // upload
        // ;
        // System.out.println(request.getSession().getServletContext()
        // .getRealPath("/")
        // + "/upload/");//
        // D:\software\eclipse\workspace2\demo_channel_terminal\src\main\webappattached/
        System.out.println("savePath:" + savePath);

        // 文件保存目录URL
        String saveUrl = /* request.getContextPath() + */"/"
                + Constant2.UPLOAD_FOLDER_NAME + "/";// /demo_channel_terminal/upload/
        if (includeContextPath) {
            saveUrl = request.getContextPath() + saveUrl;
        }
        System.out.println("saveUrl:" + saveUrl);
        // 定义允许上传的文件扩展名

        // response.setContentType("text/html; charset=UTF-8");

        if (!ServletFileUpload.isMultipartContent(request)) {
            // return new ModelAndView(new MappingJacksonJsonView(),
            // getError("请选择文件。"));
            return new UploadResult(false, false/* 是否已经保存上传的文件 */,
                    Constant2.ERROR_UPLOAD_FILE_NO_SELECTED_FILE);
        }
        // 检查目录
        File uploadDir = new File(savePath);
        if (!uploadDir.isDirectory()) {
            // return new ModelAndView(new MappingJacksonJsonView(),
            // getError("上传目录不存在。"));
            return new UploadResult(false, false/* 是否已经保存上传的文件 */, "上传目录不存在。");
        }
        // 检查目录写权限
        if (!uploadDir.canWrite()) {
            // return new ModelAndView(new MappingJacksonJsonView(),
            // getError("上传目录没有写权限。"));
            return new UploadResult(false, false/* 是否已经保存上传的文件 */, "上传目录没有写权限。");
        }

        String dirName = request.getParameter("dir");
        if (dirName == null) {
            dirName = "image";
        }
        if (!extMap.containsKey(dirName)) {
            // return new ModelAndView(new MappingJacksonJsonView(),
            // getError("目录名不正确。"));
            return new UploadResult(false, false/* 是否已经保存上传的文件 */, "目录名不正确。");
        }
        // 创建文件夹
        if (!savePath.endsWith(File.separator)) {
            savePath += File.separator;
        }
        savePath += dirName + File.separator;
        saveUrl += dirName + "/"/*File.separator*/;
        File saveDirFile = new File(savePath);
        if (!saveDirFile.exists()) {
            saveDirFile.mkdirs();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String ymd = sdf.format(new Date());
        savePath += ymd + File.separator;
        saveUrl += ymd + "/" /*File.separator*/;
        File dirFile = new File(savePath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        UploadResult uploadResult = new UploadResult(true, false/* 是否已经保存上传的文件 */);
        uploadResult.setDirName(dirName);
        uploadResult.setSavePath(savePath);
        uploadResult.setSaveUrl(saveUrl);
        return uploadResult;
    }

    private ModelAndView getModelAndView(UploadResult uploadResult) {
        return new ModelAndView(new MappingJacksonJsonView(),
                getError(uploadResult.getErrorMessage()));
    }

    @ResponseBody
    @RequestMapping(value = "/uploadKEditor", produces = SystemHWUtil.RESPONSE_CONTENTTYPE_JSON_UTF)
    public ModelAndView uploadKindEditor(
            @RequestParam(value = "imgFile", required = true) MultipartFile file,
            HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException {

        UploadResult uploadResult2 = beforeUpload(request, true);
        if (!uploadResult2.isSuccess()) {
            return getModelAndView(uploadResult2);
        }
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding("UTF-8");
        List items = upload.parseRequest(request);
        if (ValueWidget.isNullOrEmpty(items)) {
            Map map = new HashMap();
//			UploadResult uploadResult = validateUploadedFile(file.getSize(),
//					file.getOriginalFilename()/* file.getName()是不对的 */,
//					uploadResult2.getDirName(), uploadResult2.getSavePath(),
//					null, obj);
            // if (!ValueWidget.isNullOrEmpty(view)) {
            // return view;
            // }
//			if (!uploadResult.isSuccess()) {
//				return new ModelAndView(new MappingJacksonJsonView(),
//						getError(uploadResult.getErrorMessage()));
//			}
//			String newFileName = uploadResult.getErrorMessage(); // (String)
            // obj.get("url");//
            // /demo_channel_terminal/
            // upload/image/20140911/20140911175235_773.jpg
            ModelAndView mView = result22(
                    file,
                    uploadResult2.getDirName(),
                    uploadResult2.getSavePath(), map, uploadResult2);
            if (!ValueWidget.isNullOrEmpty(mView)) {
                return mView;
            }


//			
//			System.out.println("picUrl:" + picUrl);
//			System.out.println("newFileName:" + newFileName);
            return new ModelAndView(new MappingJacksonJsonView(), map);
        } else {
            Iterator itr = items.iterator();
            while (itr.hasNext()) {
                FileItem item = (FileItem) itr.next();
                String fileName = item.getName();
                long fileSize = item.getSize();
                if (!item.isFormField()) {
                    Map map = new HashMap();
                    ModelAndView mView = result22(
                            item,
                            uploadResult2.getDirName(),
                            uploadResult2.getSavePath(), map, uploadResult2);
                    if (!ValueWidget.isNullOrEmpty(mView)) {
                        return mView;
                    }

                }
            }
        }
        return new ModelAndView(new MappingJacksonJsonView(),
                getError("无上传文件。"));
    }

    private ModelAndView result22(Object fileObj,
                                  String dirName, String savePath, Map map, UploadResult uploadResult2) {
        UploadResult uploadResult = validateUploadedFile(
                fileObj,
                uploadResult2.getDirName(),
                uploadResult2.getSavePath(), map);
        if (!uploadResult.isSuccess()) {
            return new ModelAndView(new MappingJacksonJsonView(),
                    getError(uploadResult.getErrorMessage()));
        }
        String newFileName = (String) map.get("url");
        map.put("error", UPLOAD_RESULT_SUCCESS);
        String picUrl = uploadResult2.getSaveUrl() + newFileName;
        map.put("url", picUrl);
        System.out.println("newFileName:" + newFileName);
        return new ModelAndView(new MappingJacksonJsonView(), map);
    }

    /***
     * javascript alert 错误信息
     *
     * @param uploadResult2
     * @return
     */
    private String getJavascript(UploadResult uploadResult2) {
        return getJavascript(uploadResult2.getErrorMessage());
    }

    private String getJavascript(String uploadResult2) {
        String script =
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><script>alert('"
                        + SystemHWUtil.convertUTF2ISO(uploadResult2) /*
                                                             * java.net.URLEncoder
															 * .
															 * encode(,SystemHWUtil
															 * .CHARSET_UTF)
															 */
                        + "');window.parent.close_win();</script>";
        return script;
    }

    @ResponseBody
    @RequestMapping(value = "/uploadCompanyPic", produces = SystemHWUtil.RESPONSE_CONTENTTYPE_JSON_UTF)
    public String uploadCompanyPic(
            @RequestParam(value = "pic", required = true) MultipartFile file,
            HttpServletRequest request, HttpServletResponse response,
            String id_argument) throws FileUploadException {
        System.out.println(file);// org.springframework.web.multipart.commons.CommonsMultipartFile@5895fca6
        if (file == null || file.getSize() == 0) {
            return getJavascript(Constant2.ERROR_UPLOAD_FILE_NO_SELECTED_FILE);
        }
        System.out.println(file.getSize());// 若没有上传文件,则为0
        
        System.out.println(file.getOriginalFilename());// 若没有上传文件,则为空字符串
//		response.setCharacterEncoding("UTF-8");
        UploadResult uploadResult2 = beforeUpload(request, false);
        if (!uploadResult2.isSuccess()) {
            return getJavascript(uploadResult2);
        }

        Map map = new HashMap();
        // ModelAndView view
        UploadResult uploadResult = validateUploadedFile(file/* file.getName()是不对的 */,
                uploadResult2.getDirName(), uploadResult2.getSavePath(),
                map);
        // if (!ValueWidget.isNullOrEmpty(view)) {
        // return "error";
        // }
        if (!uploadResult.isSuccess()) {
            // return new ModelAndView(new MappingJacksonJsonView(),
            // getError(uploadResult.getErrorMessage()));
            return getJavascript(uploadResult);
        }
        String newFileName = (String) map.get("url");
//		String newFileName = uploadResult.getErrorMessage(); // (String)
        // obj.get("url");//
        // /demo_channel_terminal/
        // upload/image/20140911/20140911175235_773.jpg
//		if (!uploadResult.isHasUploaded()) {
//			// 保存
//			File uploadedFinalFile = new File(uploadResult2.getSavePath(),
//					newFileName);
//			try {
//				file.transferTo(uploadedFinalFile);
//				ImageHWUtil.compressImg(uploadedFinalFile, null, null);
//			} catch (Exception e) {
//				e.printStackTrace();//
//				return getJavascript("保存文件失败:file.transferTo(uploadedFinalFile)");
//			}
//		}
        String picUrl = uploadResult2.getSaveUrl() + newFileName;
        System.out.println("picUrl:" + picUrl);
        // obj.put("url", picUrl);
        // obj.put("error", 0);
        System.out.println("newFileName:" + newFileName);
        // String str="window.parent.load_pic();";
        String str = "";
        picUrl = picUrl.replace("\\", "/");
        str += "window.parent.document.getElementById('" + id_argument
                + "').value='" + picUrl + "';";
        str += "if(window.parent.document.getElementById('company_module_pic')){window.parent.document.getElementById('company_module_pic').src='"
                + request.getContextPath() + picUrl + "';}";
        System.out.println("str:" + str);
        str += SystemHWUtil.convertUTF2ISO(DictionaryParam.get(Constant2.DICTIONARY_GROUP_GLOBAL_SETTING, "upload_success_js"));

        String script = "<script>" + str + ";window.parent.close_win();</script>";
        System.out.println("uploadCompanyPic script:" + script);
        return script;
    }

    /***
     * @param itemSize
     * @param fileName
     * @param dirName
     * @param savePath
     * @param saveUrl
     * @param item
     * @param obj
     * @return
     */
    private UploadResult validateUploadedFile(Object fileObject,
                                              String dirName, String savePath, Map obj) {
        UploadResult uploadResult = new UploadResult();
        uploadResult.setSuccess(false);
        uploadResult.setHasUploaded(false);
        long itemSize;
        String fileName;
        MultipartFile mFile = null;
        FileItem item = null;
        if (fileObject instanceof MultipartFile) {
            mFile = (MultipartFile) fileObject;
            itemSize = mFile.getSize();
            fileName = mFile.getOriginalFilename();/* file.getName()是不对的 */
        } else {
            item = (FileItem) fileObject;
            itemSize = item.getSize();
            fileName = item.getName();
        }
        // 校验文件的大小
        if (itemSize == 0) {// 没有选择文件,就直接点击[提交]
            uploadResult
                    .setErrorMessage(Constant2.ERROR_UPLOAD_FILE_NO_SELECTED_FILE);
            return uploadResult;
        }
        if (itemSize > maxSize) {
            // return new ModelAndView(new MappingJacksonJsonView(),
            // getError());
            uploadResult.setErrorMessage("上传文件大小超过限制。");
            return uploadResult;
        }
        // 检查扩展名
        String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1)
                .toLowerCase();
        if (!Arrays.<String>asList(extMap.get(dirName).split(",")).contains(
                fileExt)) {
            // return new ModelAndView(new MappingJacksonJsonView(),
            // getError());
            uploadResult.setErrorMessage("上传文件扩展名是不允许的扩展名。\\n只允许"
                    + extMap.get(dirName) + "格式。");
            return uploadResult;
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String newFileName = df.format(new Date()) + "_"
                + new Random().nextInt(1000) + "." + fileExt;//保证上传后,文件的扩展名保持不变

        File uploadedFile = new File(savePath, newFileName);
        System.out.println("uploadedFile:" + uploadedFile.getAbsolutePath());
        boolean mogrifyRunSuccess = true;
        if (!ValueWidget.isNullOrEmpty(item)) {
            try {

                item.write(uploadedFile);
                ImageHWUtil.compressImg(uploadedFile, null, null);

                uploadResult.setHasUploaded(true);
            } catch (Exception e) {
                // return new ModelAndView(new MappingJacksonJsonView(),
                // getError());
                uploadResult.setErrorMessage("上传文件失败。");
                return uploadResult;
            }
        } else if (!ValueWidget.isNullOrEmpty(mFile)) {
            // 保存
//			File uploadedFinalFile = new File(uploadResult2.getSavePath(),
//					newFileName);
            try {
                mFile.transferTo(uploadedFile);
                ImageHWUtil.compressImg(uploadedFile, null, null);
                uploadResult.setHasUploaded(true);
            } catch (javax.imageio.IIOException e) {
                e.printStackTrace();
                if (e.getMessage().contains("Incompatible color conversion")) {
                    mogrifyRunSuccess = false;
                    System.out.println("mogrify run failed.");
                    CompressFailedPic compressFailedPic = new CompressFailedPic();
                    compressFailedPic.setOriginalSize(itemSize);
                    compressFailedPic.setPicPath(uploadedFile.getAbsolutePath());
                    compressFailedPic.setFailedTime(TimeHWUtil.getCurrentTimestamp());
                    compressFailedPic.setCause(e.getMessage() + " ,Localized:" + e.getLocalizedMessage());
                    this.compressFailedPicDao.add(compressFailedPic);
                }


            } catch (IOException e) {
                e.printStackTrace();
                uploadResult.setErrorMessage("保存文件失败:file.transferTo(uploadedFinalFile)" + e.getMessage());
                return uploadResult;
            }
        }
        //生成缩略图
        if (mogrifyRunSuccess && (!ValueWidget.isNullOrEmpty(uploadedFile))) {
            try {
                ImageHWUtil.thumbnail(uploadedFile.getAbsolutePath(), DictionaryParam.getGlobalSettingInt("thumbnail_px"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // obj.put("url", newFileName);
        uploadResult.setSuccess(true);
        obj.put("url", newFileName);
//		uploadResult.setErrorMessage(newFileName);
        return uploadResult;
    }

    private Map getError(String message) {
        Map errorMap = new HashMap();
        errorMap.put("error", UPLOAD_RESULT_FAILED);
        errorMap.put("message", message);
        System.out.println("map:" + errorMap);
        return errorMap;
    }


    @RequestMapping(value = "/fileManager", produces = SystemHWUtil.RESPONSE_CONTENTTYPE_JSON_UTF)
    public String fileManager(HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        // 根目录路径，可以指定绝对路径，比如 /var/www/attached/
        String rootPath = WebServletUtil.getUploadedFilePath(request,
                Constant2.UPLOAD_FOLDER_NAME + File.separator,
                Constant2.SRC_MAIN_WEBAPP);// request.getSession().getServletContext().getRealPath("/")
        // + "attached/";
        // 根目录URL，可以指定绝对路径，比如 http://www.yoursite.com/attached/
        System.out.println("rootPath:" + rootPath);
        String rootUrl = request.getContextPath() + "/" /*File.separator*/
                + Constant2.UPLOAD_FOLDER_NAME + "/" /*File.separator*/;
        // 图片扩展名
        String[] fileTypes = new String[]{"gif", "jpg", "jpeg", "png", "bmp",
                "GIF", "JPG", "JPEG", "PNG", "BMP"};
        PrintWriter out = response.getWriter();
        String dirName = request.getParameter("dir");
        if (dirName != null) {
            if (!Arrays.<String>asList(
                    new String[]{"image", "flash", "media", "file"})
                    .contains(dirName)) {
                out.println("Invalid Directory name.");
                return null;
            }
            if (!rootPath.endsWith(File.separator)) {
                rootPath += File.separator;
            }
            rootPath += dirName + File.separator;
            rootUrl += dirName + "/" /*File.separator*/;
            File saveDirFile = new File(rootPath);
            if (!saveDirFile.exists()) {
                saveDirFile.mkdirs();
            }
        }
        // 根据path参数，设置各路径和URL
        String path = request.getParameter("path") != null ? request
                .getParameter("path") : "";
        String currentPath = rootPath + path;
        String currentUrl = rootUrl + path;
        String currentDirPath = path;
        String moveupDirPath = "";
        if (!"".equals(path)) {
            String str = currentDirPath.substring(0,
                    currentDirPath.length() - 1);
            moveupDirPath = str.lastIndexOf(File.separator) >= 0 ? str
                    .substring(0, str.lastIndexOf(File.separator) + 1) : "";
        }

        // 排序形式，name or size or type
        String order = request.getParameter("order") != null ? request
                .getParameter("order").toLowerCase() : "name";

        // 不允许使用..移动到上一级目录
        if (path.indexOf("..") >= 0) {
            out.println("Access is not allowed.");
            return null;
        }
        // 最后一个字符不是/
        if (!"".equals(path) && !path.endsWith("/")) {// 注意:这里不能是"\\"
            out.println("Parameter is not valid.");
            return null;
        }
        // 目录不存在或不是目录
        File currentPathFile = new File(currentPath);
        if (!currentPathFile.isDirectory()) {
            out.println("Directory does not exist.");
            return null;
        }

        // 遍历目录取的文件信息
        List<Hashtable> fileList = new ArrayList<Hashtable>();
        File[] files = currentPathFile.listFiles();
        if (files != null) {
            for (File file : files) {
                Hashtable<String, Object> hash = new Hashtable<String, Object>();
                String fileName = file.getName();
                if (file.isDirectory()) {
                    hash.put("is_dir", true);
                    hash.put("has_file", (file.listFiles() != null));
                    hash.put("filesize", 0L);
                    hash.put("is_photo", false);
                    hash.put("filetype", "");
                } else if (file.isFile()) {
                    String fileExt = fileName.substring(
                            fileName.lastIndexOf(".") + 1).toLowerCase();
                    hash.put("is_dir", false);
                    hash.put("has_file", false);
                    hash.put("filesize", file.length());
                    hash.put("is_photo", Arrays.<String>asList(fileTypes)
                            .contains(fileExt));
                    hash.put("filetype", fileExt);
                }
                hash.put("filename", fileName);
                hash.put("datetime",
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file
                                .lastModified()));
                fileList.add(hash);
            }
        }

        if ("size".equals(order)) {
            Collections.sort(fileList, new SizeComparator());
        } else if ("type".equals(order)) {
            Collections.sort(fileList, new TypeComparator());
        } else {
            Collections.sort(fileList, new NameComparator());
        }
        JSONObject result = new JSONObject();
        result.put("moveup_dir_path", moveupDirPath);
        result.put("current_dir_path", currentDirPath);
        result.put("current_url", currentUrl);
        result.put("total_count", fileList.size());
        result.put("file_list", fileList);

        response.setContentType("application/json; charset=UTF-8");
        out.println(result.toString());
        return null;

    }

    public CompressFailedPicDao getCompressFailedPicDao() {
        return compressFailedPicDao;
    }

    @Resource
    public void setCompressFailedPicDao(CompressFailedPicDao compressFailedPicDao) {
        this.compressFailedPicDao = compressFailedPicDao;
    }

    @Override
    protected void beforeAddInput(Model model) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void errorDeal(Model model) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getJspFolder() {
        // TODO Auto-generated method stub
        return null;
    }

    public static class NameComparator implements Comparator {
        public int compare(Object a, Object b) {
            Hashtable hashA = (Hashtable) a;
            Hashtable hashB = (Hashtable) b;
            if (((Boolean) hashA.get("is_dir"))
                    && !((Boolean) hashB.get("is_dir"))) {
                return -1;
            } else if (!((Boolean) hashA.get("is_dir"))
                    && ((Boolean) hashB.get("is_dir"))) {
                return 1;
            } else {
                return ((String) hashA.get("filename"))
                        .compareTo((String) hashB.get("filename"));
            }
        }
    }

    public static class SizeComparator implements Comparator {
        public int compare(Object a, Object b) {
            Hashtable hashA = (Hashtable) a;
            Hashtable hashB = (Hashtable) b;
            if (((Boolean) hashA.get("is_dir"))
                    && !((Boolean) hashB.get("is_dir"))) {
                return -1;
            } else if (!((Boolean) hashA.get("is_dir"))
                    && ((Boolean) hashB.get("is_dir"))) {
                return 1;
            } else {
                if (((Long) hashA.get("filesize")) > ((Long) hashB
                        .get("filesize"))) {
                    return 1;
                } else if (((Long) hashA.get("filesize")) < ((Long) hashB
                        .get("filesize"))) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    public static class TypeComparator implements Comparator {
        public int compare(Object a, Object b) {
            Hashtable hashA = (Hashtable) a;
            Hashtable hashB = (Hashtable) b;
            if (((Boolean) hashA.get("is_dir"))
                    && !((Boolean) hashB.get("is_dir"))) {
                return -1;
            } else if (!((Boolean) hashA.get("is_dir"))
                    && ((Boolean) hashB.get("is_dir"))) {
                return 1;
            } else {
                return ((String) hashA.get("filetype"))
                        .compareTo((String) hashB.get("filetype"));
            }
        }
    }

}
