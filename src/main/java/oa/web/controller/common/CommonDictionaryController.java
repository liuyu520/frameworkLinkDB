package oa.web.controller.common;

import com.common.bean.ClientOsInfo;
import com.common.dict.Constant2;
import com.common.util.SystemHWUtil;
import com.common.util.WebServletUtil;
import com.io.hw.json.HWJacksonUtils;
import com.string.widget.util.ValueWidget;
import oa.entity.common.AccessLog;
import oa.entity.common.CommonDictionary;
import oa.service.DictionaryParam;
import oa.view.CommonDictionaryView;
import oa.web.controller.base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/dictionary")
public class CommonDictionaryController extends BaseController<CommonDictionary> {
    private String label = "dictionary";
//	private String redirectViewAll = "redirect:/dictionary/list";

    @ResponseBody
    @RequestMapping(value = "/json_by_group", produces = SystemHWUtil.RESPONSE_CONTENTTYPE_JSON_UTF)
    public String jsonByGroup(Model model, CommonDictionary commonDictionary,
                              CommonDictionaryView view, HttpSession session,
                              HttpServletRequest request, String callback, String osType, String deviceId) throws IOException {
        init(request);
        String content = null;
        String groupId = commonDictionary.getGroupId();
        Map<String, String> map = null;
        if (ValueWidget.isNullOrEmpty(groupId)) {
            map = new HashMap<String, String>();//空map,比直接空字符串要好些,便于前端判断
            //返回的结果:{}
        } else {
            map = DictionaryParam.getIdValue(groupId);
            //返回:{"3":"足球","2":"体育","1":"娱乐","7":"篮球2","6":"篮球","5":"NBA22","4":"NBA","9":"篮球231","8":"篮球23"}
        }
        content = HWJacksonUtils.getJsonP(map, callback);
        AccessLog accessLog = new AccessLog();
        accessLog.setAccessType(Constant2.LOGS_ACCESS_TYPE_INTO);
        accessLog.setRequestTarget(getJspFolder() + ":" + groupId);
        ClientOsInfo info = WebServletUtil.getMobileOsInfo(request);
        accessLog.setUserAgent(info.getUserAgent());
        accessLog.setDeviceType(info.getDeviceType());//Pad或Phone
        if (!ValueWidget.isNullOrEmpty(osType)) {
            accessLog.setOsType(osType);
        } else {
            accessLog.setOsType(info.getOsType());
        }
        if (!ValueWidget.isNullOrEmpty(deviceId)) {
            accessLog.setDeviceId(deviceId);
        }
        if (!ValueWidget.isNullOrEmpty(accessLog)) {
            accessLog.setDescription("访问数据字典");
            accessLog.setOperateResult(String.format(Constant2.RECORD_TOTAL_SUM, map.size()));
            super.logSave(accessLog, request);
        }

        return content;
    }


    @Override
    protected void beforeAddInput(Model model) {

    }

    @Override
    protected void errorDeal(Model model) {

    }

    @Override
    public String getJspFolder() {
        return label;
    }

}
