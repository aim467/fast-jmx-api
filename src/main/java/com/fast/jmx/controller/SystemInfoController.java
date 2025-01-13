package com.fast.jmx.controller;

import com.fast.jmx.domain.R;
import com.fast.jmx.domain.oshi.DashBordInfo;
import com.fast.jmx.domain.oshi.OSInfo;
import com.fast.jmx.domain.oshi.OSRuntimeInfo;
import com.fast.jmx.utils.SystemInfoUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class SystemInfoController {


    @GetMapping("/systemInfo")
    public R<DashBordInfo> getSystemInfo() throws InterruptedException {
        OSInfo osInfo = SystemInfoUtil.getSystemInfo();
        OSRuntimeInfo osRuntimeInfo = SystemInfoUtil.getOSRuntimeInfo();
        DashBordInfo monitorInfo = new DashBordInfo();
        monitorInfo.setOsInfo(osInfo);
        monitorInfo.setOsRuntimeInfo(osRuntimeInfo);
        return R.ok(monitorInfo);
    }
}
