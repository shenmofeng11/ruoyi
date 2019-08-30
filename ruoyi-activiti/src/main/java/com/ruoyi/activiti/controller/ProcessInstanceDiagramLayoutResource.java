//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ruoyi.activiti.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activiti/service")
public class ProcessInstanceDiagramLayoutResource extends BaseProcessDefinitionDiagramLayoutResource {
    public ProcessInstanceDiagramLayoutResource() {
    }

    @RequestMapping(
            value = {"/process-instance/{processInstanceId}/diagram-layout"},
            method = {RequestMethod.GET},
            produces = {"application/json"}
    )
    public ObjectNode getDiagram(@PathVariable String processInstanceId) {
        return this.getDiagramNode(processInstanceId, (String)null);
    }
}
