/* global jQuery, echarts */
(function ($) {
    $.fn.extend({
        /**
         * Renders a histogram using ECharts.
         *
         * @param {String} model - the chart model
         * @param {String} metricName - the name of the metric, used e.g. as title for the graph
         * @param {String} logarithmicCheckboxId - the id of the checkbox to use for toggling logarithmic scaling
         */
        renderHistogram: function (model, metricName, logarithmicCheckboxId) {
            var histogramData = JSON.parse(model);
            // replace all `0`s with `null` to enable logarithmic axis scaling
            if (histogramData && histogramData.data) {
                histogramData.data = histogramData.data.map(x => x > 0 ? x : null);
            }
            var chart = echarts.init($(this)[0], 'light');
            var options = {
                title: {
                    text: 'Histogram of "' + metricName + '"',
                    left: 'center'
                },
                tooltip: {
                    trigger: 'axis',
                    formatter: '<b>' + metricName + ': {b}</b><br/>{c} Classes'
                },
                xAxis: {
                    name: metricName,
                    nameLocation: 'center',
                    nameGap: 30,
                    type: 'category',
                    data: histogramData.labels
                },
                yAxis: {
                    name: 'Number of Classes',
                    type: 'value'
                },
                series: [{
                    data: histogramData.data,
                    type: 'bar'
                }]
            };
            chart.setOption(options);
            chart.resize();
            $(window).on('resize', function () {
                chart.resize();
            });
            $(this).data('chart', chart);
            $(logarithmicCheckboxId).change(function () {
                const checkBox = $(this);
                if (checkBox.is(':checked')) {
                    chart.setOption({
                        yAxis: {
                            name: 'Number of Classes',
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
                            name: 'Number of Classes',
                            type: 'value'
                        }
                    })
                }
            });
        }
    });
})(jQuery);
