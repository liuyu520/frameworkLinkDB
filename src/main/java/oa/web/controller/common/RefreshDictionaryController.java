package oa.web.controller.common;

import com.common.util.SystemHWUtil;
import oa.service.DictionaryParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@RequestMapping("/refresh")
public class RefreshDictionaryController {
    private DictionaryParam dictionaryParam;

    @RequestMapping(value = "/refresh")
    public String refresh(HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
//		DictionaryParam.refresh();
        this.dictionaryParam.refresh2();
        out.println("<html>Refresh successfully!");
        out.println("<script type=\"text/javascript\" charset=\"UTF-8\" >");
        out.println("setTimeout(function(){history.go(-1);},2000);");
        out.println("</script></html>");
        return null;
    }

    @ResponseBody
    @RequestMapping(value = "/json", produces = SystemHWUtil.RESPONSE_CONTENTTYPE_JSON_UTF)
    public String json(HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
//		DictionaryParam.refresh();
        this.dictionaryParam.refresh2();
        out.println("{\"result\":1}");
        return null;
    }

    public DictionaryParam getDictionaryParam() {
        return dictionaryParam;
    }

    @Resource
    public void setDictionaryParam(DictionaryParam dictionaryParam) {
        this.dictionaryParam = dictionaryParam;
    }


}
