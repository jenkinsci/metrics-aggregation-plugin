/* global jQuery, echarts */
(function ($) {
    $.fn.extend({
        /**
         * Renders a scatter plot using ECharts.
         *
         * @param {String} model - the chart model
         * @param {String} metricNameX - the name of the metric on the x-axis, used e.g. as title for the graph
         * @param {String} metricNameY - the name of the metric on the y-axis, used e.g. as title for the graph
         */
        renderScatterPlot: function (model, metricNameX, metricNameY) {
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
                    nameGap: 30
                },
                yAxis: {
                    name: metricNameY
                },
                series: [{
                    data: JSON.parse(model),
                    type: 'scatter'
                }]
            };
            chart.setOption(options);
            chart.resize();
            $(window).on('resize', function () {
                chart.resize();
            });
            $(this).data('chart', chart);
        }
    });
})(jQuery);
