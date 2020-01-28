/* global jQuery, echarts */
(function ($) {
    $.fn.extend({
        /**
         * Renders a scatter plot using ECharts.
         *
         * @param {String} model - the chart model
         * @param {String} metricNameX - the name of the metric on the x-axis, used e.g. as title for the graph
         * @param {String} metricNameY - the name of the metric on the y-axis, used e.g. as title for the graph
         * @param {String} logarithmicCheckboxIdX - the id of the checkbox to use for toggling logarithmic scaling on
         *     the x-axis
         * @param {String} logarithmicCheckboxIdY - the id of the checkbox to use for toggling logarithmic scaling on
         *     the y-axis
         */
        renderScatterPlot: function (model, metricNameX, metricNameY, logarithmicCheckboxIdX, logarithmicCheckboxIdY) {
            var scatterPlotData = JSON.parse(model);
            // replace all `0`s and `NaN`s with `null` to enable logarithmic axis scaling
            if (scatterPlotData) {
                scatterPlotData = scatterPlotData.map(x => ({
                    name: x.name,
                    value: x.value.map(v => v > 0 ? v : null)
                }));
            }
            var chart = echarts.init($(this).get(0), 'light');
            var options = {
                title: {
                    text: '"' + metricNameX + '" vs "' + metricNameY + '"',
                    left: 'center'
                },
                tooltip: {
                    trigger: 'item',
                    formatter: function (data, _ticket, _callback) {
                        return '<b>' + data.name + '</b><br/>' +
                            metricNameX + ': ' + data.value[0] + '<br/>' +
                            metricNameY + ': ' + data.value[1];
                    }
                },
                xAxis: {
                    name: metricNameX,
                    nameLocation: 'center',
                    nameGap: 30,
                    type: 'value'
                },
                yAxis: {
                    name: metricNameY,
                    type: 'value'
                },
                series: [{
                    data: scatterPlotData,
                    type: 'scatter'
                }]
            };
            chart.setOption(options);
            chart.resize();
            $(window).on('resize', function () {
                chart.resize();
            });
            $(this).data('chart', chart);
            $(logarithmicCheckboxIdX).change(function () {
                const checkBox = $(this);
                if (checkBox.is(':checked')) {
                    chart.setOption({
                        xAxis: {
                            name: metricNameX,
                            nameLocation: 'center',
                            nameGap: 30,
                            type: 'log',
                            minorTick: {
                                show: true
                            },
                            minorSplitLine: {
                                show: true
                            }
                        }
                    })
                } else {
                    chart.setOption({
                        xAxis: {
                            name: metricNameX,
                            nameLocation: 'center',
                            nameGap: 30,
                            type: 'value'
                        }
                    })
                }
            });
            $(logarithmicCheckboxIdY).change(function () {
                const checkBox = $(this);
                if (checkBox.is(':checked')) {
                    chart.setOption({
                        yAxis: {
                            name: metricNameY,
                            type: 'log',
                            minorTick: {
                                show: true
                            },
                            minorSplitLine: {
                                show: true
                            }
                        }
                    })
                } else {
                    chart.setOption({
                        yAxis: {
                            name: metricNameY,
                            type: 'value'
                        }
                    })
                }
            });
        }
    });
})(jQuery);
