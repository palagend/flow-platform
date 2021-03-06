/*
 * Copyright 2017 flow.ci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flow.platform.api.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flow.platform.api.domain.node.Node;
import com.flow.platform.plugin.domain.Plugin;
import com.flow.platform.plugin.domain.PluginDetail;
import com.flow.platform.plugin.domain.PluginStatus;
import com.flow.platform.plugin.domain.envs.PluginProperty;
import com.flow.platform.plugin.domain.envs.PluginPropertyType;
import com.flow.platform.plugin.service.PluginService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * @author yang
 */
public class StepControllerTest extends ControllerTestWithoutAuth {

    private final String flowName = "flow_default";

    @Autowired
    private PluginService pluginService;

    @Before
    public void init() throws Throwable {
        stubDemo();
        createEmptyFlow(flowName);

        Plugin plugin = new Plugin("fir-cli", "xx", ImmutableSet.of("fir"), "xx", ImmutableSet.of("*"));
        plugin.setPluginDetail(new PluginDetail("fir-cli", "fir upload xx"));
        plugin.getPluginDetail().getProperties().add(new PluginProperty("FIR_TOKEN", PluginPropertyType.STRING, true));
        plugin.getPluginDetail().getProperties().add(new PluginProperty("FIR_PATH", PluginPropertyType.STRING, true));

        Mockito.when(pluginService.list(ImmutableSet.of(PluginStatus.INSTALLED), null, null))
            .thenReturn(ImmutableList.of(plugin));
    }

    @Test
    public void should_list_children_node_of_root() throws Throwable {
        // given:
        createYml(flowName, "yml/for_flow_update.yml");

        // when:
        MvcResult result = mockMvc.perform(get("/flows/" + flowName + "/steps")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        // then:
        Node[] children = Node.parseArray(result.getResponse().getContentAsString().getBytes(), Node[].class);
        Assert.assertNotNull(children);
        Assert.assertEquals(3, children.length);

        // then:
        Node firstStep = children[0];
        Assert.assertEquals("step1", firstStep.getName());
        Assert.assertEquals("echo 1", nodeService.getRunningScript(firstStep));
        Assert.assertEquals(true, firstStep.getAllowFailure());

        // then:
        Node secondStep = children[1];
        Assert.assertEquals("step2", secondStep.getName());
        Assert.assertNull(secondStep.getScript());
        Assert.assertEquals("fir-cli", secondStep.getPlugin());
        Assert.assertEquals(false, secondStep.getAllowFailure());
        Assert.assertEquals("firtoken", secondStep.getEnv("FIR_TOKEN"));
        Assert.assertEquals("${HOME}/PATH", secondStep.getEnv("FIR_PATH"));

        // then:
        Node thirdStep = children[2];
        Assert.assertEquals("step3", thirdStep.getName());
        Assert.assertEquals("echo 3", nodeService.getRunningScript(thirdStep));
        Assert.assertEquals(false, thirdStep.getAllowFailure());
        Assert.assertEquals("AA", thirdStep.getEnv("FLOW_A"));
        Assert.assertEquals("BB", thirdStep.getEnv("FLOW_B"));
    }
}
