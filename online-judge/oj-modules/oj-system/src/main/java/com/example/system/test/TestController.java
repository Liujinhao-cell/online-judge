package com.example.system.test;

import com.example.common.core.domain.R;
import com.example.common.core.enums.ResultCode;
import com.example.system.test.domain.LoginTestDTO;
import com.example.system.test.service.ITestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private ITestService testService;
    @GetMapping("/list")
    public List<?> list(){
        return testService.list();
    }
    @GetMapping("/add")
    public String add(){
        return testService.add();
    }
    @GetMapping("/apifoxtest")
    public R<String> apifoxtest(String apIId){
        R<String> result = new R<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(apIId);
        result.setData(apIId);
        return result;
    }
    @PostMapping("/apifoxPost")
    public R<String> apifoxtest(@RequestBody LoginTestDTO loginTestDTO){
        R<String> result = new R<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData("apifoxPost:"+loginTestDTO.getUserAccount() +":"+loginTestDTO.getPassword());
        return result;
    }
    @GetMapping("/log")
    public String log(){
        log.info("info级别");
        log.error("error级别日志");
        return "日志测试";
    }
}
