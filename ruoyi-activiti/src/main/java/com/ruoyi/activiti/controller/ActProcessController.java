package com.ruoyi.activiti.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruoyi.activiti.domain.ProcessDefinitionDto;
import com.ruoyi.activiti.service.ActProcessService;
import com.ruoyi.activiti.utils.ActUtils;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.util.ShiroUtils;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程管理 操作处理
 * 
 * @author ruoyi
 */
@Controller
@RequestMapping("/activiti/process")
public class ActProcessController extends BaseController
{
    private String prefix = "activiti/process";

    @Autowired
    private ActProcessService actProcessService;
    @Autowired
    private FormService formService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;

    @RequiresPermissions("activiti:process:view")
    @GetMapping
    public String process()
    {
        return prefix + "/process";
    }

    @RequiresPermissions("activiti:process:list")
    @PostMapping("list")
    @ResponseBody
    public TableDataInfo list(ProcessDefinitionDto processDefinitionDto)
    {
        return actProcessService.selectProcessDefinitionList(processDefinitionDto);
    }

    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("activiti:process:add")
    @Log(title = "流程管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@RequestParam String category, @RequestParam("file") MultipartFile file)
            throws IOException
    {
        InputStream fileInputStream = file.getInputStream();
        String fileName = file.getOriginalFilename();
        return actProcessService.saveNameDeplove(fileInputStream, fileName, category);
    }

    @RequiresPermissions("activiti:process:model")
    @GetMapping(value = "/convertToModel/{processId}")
    @ResponseBody
    public AjaxResult convertToModel(@PathVariable("processId") String processId)
    {
        try
        {
            Model model = actProcessService.convertToModel(processId);
            return success(StringUtils.format("转换模型成功，模型编号[{}]", model.getId()));
        }
        catch (Exception e)
        {
            return error("转换模型失败" + e.getMessage());
        }
    }

    @GetMapping(value = "/resource/{imageName}/{deploymentId}")
    public void viewImage(@PathVariable("imageName") String imageName,
            @PathVariable("deploymentId") String deploymentId, HttpServletResponse response)
    {
        try
        {
            InputStream in = actProcessService.findImageStream(deploymentId, imageName);
            for (int bit = -1; (bit = in.read()) != -1;)
            {
                response.getOutputStream().write(bit);
            }
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @RequiresPermissions("activiti:process:remove")
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(actProcessService.deleteProcessDefinitionByDeploymentIds(ids));
    }

    //yxf 以下
    /**
     * 自定义表单
     * @param procDefId
     * @param mmap
     * @return
     */
    @GetMapping("startFrom/{procDefId}")
    public String customForm(@PathVariable("procDefId") String procDefId, ModelMap mmap) {
        StartFormData startFormData = formService.getStartFormData(procDefId);
        ProcessDefinition pd = startFormData.getProcessDefinition();
        mmap.put("pd", pd);
        if(pd.hasStartFormKey()){//判断是不是有外置表单
            //有外置表单情况下,拿取启动的表单数据
            Object renderedStartForm = formService.getRenderedStartForm(procDefId);//返回的纯文本的html代码。
            mmap.put("html", renderedStartForm);
            return prefix + "/startOutlayForm";
        }else{//自定义表单
            List<FormProperty> formProperties = startFormData.getFormProperties();
            mmap.put("list", formProperties);
            return prefix + "/startCustomForm";
        }

    }

    @PostMapping("start")
    @ResponseBody
    public AjaxResult start(String procDefId,String procName,@RequestParam Map<String,Object> map, ModelMap mmap)
    {
        //设置流程发起人
        identityService.setAuthenticatedUserId(ShiroUtils.getLoginName());
        //启动流程
        ProcessInstance pi = runtimeService.startProcessInstanceById(procDefId,map);
        //设置流程实例的名字
        runtimeService.setProcessInstanceName(pi.getId(),procName);
        return AjaxResult.success("操作成功");
    }

    /**
     * 跳转的历史流程页面
     * @return
     */
    @GetMapping("history")
    public String history() {
        return prefix + "/history";
    }

    /**
     * 查询由某人发起的流程请求
     * @param pageNum
     * @param pageSize
     * @return
     */
    @PostMapping("historyList")
    @ResponseBody
    public TableDataInfo historyList(int pageNum,int pageSize){
        pageNum = (pageNum - 1) * pageSize;
        if (pageNum < 0){  pageNum = 0;}

        HistoricProcessInstanceQuery hpQuery = historyService.createHistoricProcessInstanceQuery();
        hpQuery.startedBy(ShiroUtils.getLoginName());
        TableDataInfo data = new TableDataInfo();
        data.setTotal(hpQuery.count());
        data.setRows(hpQuery.orderByProcessInstanceStartTime().desc().listPage(pageNum, pageSize));
        return data;
    }

    /**
     * 跳转的代办任务列表页面
     * @return
     */
    @GetMapping("task")
    public String task() {
        return prefix + "/task";
    }

    /**
     * 查询代办任务列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @PostMapping("taskList")
    @ResponseBody
    public TableDataInfo taskList(int pageNum,int pageSize){
        pageNum = (pageNum - 1) * pageSize;
        if (pageNum < 0){  pageNum = 0;}
        //获取代办任务
        TaskQuery tq = taskService.createTaskQuery();
        tq.taskCandidateOrAssigned(ShiroUtils.getLoginName()).active();
        List<Task> list = tq.orderByTaskCreateTime().desc().listPage(pageNum, pageSize);
        List<Map<String, Object>> newList = new ArrayList<Map<String,Object>>();
        for(Task task : list){
            //查询流程实例信息
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();

            Map<String, Object> values = new HashMap<String,Object>();
            values.put("id", task.getId());
            values.put("name", task.getName());
            values.put("createTime", task.getCreateTime());
            values.put("procName", pi.getName());
            values.put("startUserId", hpi.getStartUserId());
            values.put("startTime", hpi.getStartTime());
            newList.add(values);
        }
        TableDataInfo data = new TableDataInfo();
        data.setTotal(tq.count());
        data.setRows(newList);
        return data;
    }

    /**
     * 跳转的代办任务页面
     * @return
     */
    @GetMapping("completeForm/{id}")
    public String completeForm(@PathVariable("id")String id, ModelMap mmap) {
        Task task = this.taskService.createTaskQuery().taskId(id).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        TaskFormData taskFormData = this.formService.getTaskFormData(id);
        ProcessInstance pi = this.runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        mmap.put("task", task);
        mmap.put("pi", pi);
        if(taskFormData.getFormKey() != null){//判断是不是有外置表单
            Object renderedStartForm = formService.getRenderedTaskForm(id);
            mmap.put("html", renderedStartForm);
            return prefix + "/completeOutlayForm";
        }else{//自定义表单
            List<FormProperty> list = taskFormData.getFormProperties();
            mmap.put("list", list);
            return prefix + "/completeCustomForm";
        }

    }

    /**
     * 办理任务
     * @param taskId
     * @param variables
     * @param mmap
     * @return
     */
    @PostMapping("complete")
    @ResponseBody
    public AjaxResult complete(String taskId,@RequestParam Map<String,Object> variables, ModelMap mmap)
    {
        taskService.setVariablesLocal(taskId,variables);
        taskService.complete(taskId,variables);
        return AjaxResult.success("操作成功");
    }
    /**
     * 根据流程实例Id,获取实时流程图片
     * @param processInstanceId
     * @param response
     */
    @GetMapping("getFlowImg/{processInstanceId}")
    public void getFlowImgByInstantId(@PathVariable("processInstanceId") String processInstanceId, HttpServletResponse response) {
        try {
            System.out.println("processInstanceId:" + processInstanceId);
            ActUtils.getFlowImgByInstanceId(processInstanceId, response.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 跳转的流程图跟踪页面
     * @return
     */
    @GetMapping("toFlowImg")
    public String toFlowImg(String processDefinitionId,String processInstanceId, ModelMap mmap) {
        mmap.put("processDefinitionId", processDefinitionId);
        mmap.put("processInstanceId", processInstanceId);
        return "activiti/diagram-viewer/index.html";
    }

    /**
     * 查询自定义表单历史
     * @param taskDefinitionKey
     * @param processInstanceId
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("getFormProperties")
    @ResponseBody
    public AjaxResult getFormProperties(String taskDefinitionKey, String processInstanceId)throws JsonProcessingException {
        List<List<HistoricVariableInstance>> listAll = new ArrayList<List<HistoricVariableInstance>>();

        List<HistoricTaskInstance> hisTaskList = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).taskDefinitionKey(taskDefinitionKey).list();
        for (HistoricTaskInstance hisTask : hisTaskList){
            List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery().taskId(hisTask.getId()).list();
            if(list.size()>0){
                listAll.add(list);
            }
        }

        return AjaxResult.success("操作成功",listAll);
    }

    /**
     * 删除流程实例
     * @param processInstanceId
     * @return
     */
    @GetMapping("deleteProcess")
    @ResponseBody
    public AjaxResult deleteProcess(String processInstanceId){
        ProcessInstance pi = this.runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if(pi==null){
            System.out.println("流程已经结束");
            historyService.deleteHistoricProcessInstance(processInstanceId);
        }
        else{
            System.out.println("流程没有结束");
            runtimeService.deleteProcessInstance(processInstanceId,"processInstance delete");//先结束流程
            historyService.deleteHistoricProcessInstance(processInstanceId);
        }
        return AjaxResult.success("操作成功");
    }
}
