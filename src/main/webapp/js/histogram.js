/* global jQuery, echarts */
(function ($) {
    $.fn.extend({
        /**
         * Renders a histogram using ECharts.
         *
         * @param {String} model - the chart model
         * @param {String} metricName - the name of the metric, used e.g. as title for the graph
         */
        renderHistogram: function (model, metricName) {
            var histogramData = JSON.parse(model);
            var chart = echarts.init($(this)[0], 'light');
            var options = {
                title: {
                    text: 'Histogram of ' + metricName,
                    left: 'center'
                },
                tooltip: {
                    trigger: 'axis',
                    formatter: '<b>' + metricName + ': {b}</b><br/>{c} Classes'
                },
                xAxis: {
                    name: metricName,
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
        }
    });
})(jQuery);
