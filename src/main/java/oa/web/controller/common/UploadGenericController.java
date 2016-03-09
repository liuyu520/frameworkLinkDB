package oa.web.controller.common;

import com.common.util.SystemHWUtil;
import com.string.widget.util.ValueWidget;
import oa.entity.common.AccessLog;
import oa.web.controller.base.BaseController;
import oa.web.upload.UploadCallback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/***
 * 上传文件通用类
 *
 * @author huangweii
 *         2015年11月10日
 */
public abstract class UploadGenericController extends BaseController {
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
//			System.out.println("request:" + request);// org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest@7063d827
//			System.out.println("request:" + request.getClass().getSuperclass());
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
            String errorMessage = filterFileSize(file, request, accessLog, errorPrefix);
            if (errorMessage != null) return errorMessage;
            fileName = fileName.replaceAll("[\\s]+", SystemHWUtil.EMPTY);//IE中识别不了有空格的json


            UploadCallback uploadCallback = getUploadCallback();
            try {
                return uploadCallback.callback(file, request, response);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
//		ModelAndView modelAndView = new ModelAndView(
//				new MappingJacksonJsonView(), map);
/*
 * {"fileName":"20141002125209_571slide4.jpg","path":"D:\\software\\eclipse\\workspace2\\demo_channel_terminal\\upload\\image\\20141002125209_571slide4.jpg"}
 * */
        return content;

    }

    private String filterFileSize(@RequestParam(value = "image223", required = false) MultipartFile file, HttpServletRequest request, AccessLog accessLog, String errorPrefix) {
        if (file.getSize() == 0) {
            String errorMessage = errorPrefix + "file size is zero";
            if (!ValueWidget.isNullOrEmpty(accessLog)) {
                accessLog.setOperateResult(errorMessage);
                logSave(accessLog, request);
            }

            return errorMessage;
        }
        return null;
    }

    public abstract UploadCallback getUploadCallback();
}
