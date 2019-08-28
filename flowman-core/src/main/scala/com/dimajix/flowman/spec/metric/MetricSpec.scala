/*
 * Copyright 2019 Kaya Kupferschmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dimajix.flowman.spec.metric

import com.fasterxml.jackson.annotation.JsonProperty

import com.dimajix.flowman.execution.Context
import com.dimajix.flowman.metric.MetricBoard
import com.dimajix.flowman.metric.MetricBundle
import com.dimajix.flowman.metric.MetricSystem
import com.dimajix.flowman.metric.Selector
import com.dimajix.flowman.metric.SelectorMetricBundle


class MetricSpec {
    @JsonProperty(value = "name", required = true) var name:String = _
    @JsonProperty(value = "labels", required = false) var labels:Map[String,String] = Map()
    @JsonProperty(value = "selector", required = true) var selector:SelectorSpec = _

    def instantiate(context:Context, metricSystem: MetricSystem) : MetricBundle = {
        def relabel(metrticLabels:Map[String,String]) = context.evaluate(labels, metrticLabels)

        new SelectorMetricBundle(
            context.evaluate(name),
            labels,
            metricSystem,
            selector.instantiate(context),
            relabel
        )
    }
}


class SelectorSpec {
    @JsonProperty(value = "name", required = true) var name: Option[String] = None
    @JsonProperty(value = "labels", required = true) var labels: Map[String, String] = Map()

    def instantiate(context: Context): Selector = {
        Selector(
            name.map(context.evaluate),
            context.evaluate(labels)
        )
    }
}



class MetricBoardSpec {
    @JsonProperty(value = "labels", required = true) private var labels: Map[String, String] = Map()
    @JsonProperty(value = "metrics", required = true) private var metrics: Seq[MetricSpec] = Seq()

    def instantiate(context: Context, metricSystem: MetricSystem): MetricBoard = {
        MetricBoard(
            context.evaluate(labels),
            metrics.map(_.instantiate(context, metricSystem))
        )
    }
}