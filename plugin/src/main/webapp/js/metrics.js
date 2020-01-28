/* global jQuery, view */
(function ($) {
    $(document).ready(function () {

        /* ------------------------------------------------------------------------------
                                            table
         ------------------------------------------------------------------------------ */

        view.getSupportedMetrics(function (res) {
            //$('#metrics-table').renderMetricsTable(res.responseJSON);
            $('#table-filter').filterTable(res.responseJSON || [], $('#metrics-table'));
        });

        /* ------------------------------------------------------------------------------
                                            charts
         ------------------------------------------------------------------------------ */

        function drawCharts() {
            var metric = $('#treechart-picker').val();            
            var metricId = $('#treechart-picker :selected').text();

            view.getMetricsTree(metric, function (res) {
                $('#treechart').renderTreeChart(res.responseJSON, metricId);
            });

            view.getHistogram(metric, function (res) {
                $('#histogram').renderHistogram(res.responseJSON, metricId, '#histogram-checkbox-log');
            });

            var secondMetric = $('#scatterplot-picker').val();
            var secondMetricId = $('#scatterplot-picker :selected').text();
            view.getScatterPlot(metric, secondMetric, function (res) {
                $('#scatterplot').renderScatterPlot(res.responseJSON, metricId, secondMetricId,
                    '#scatterplot-checkbox-log-x', '#scatterplot-checkbox-log-y');
            });
        }

        drawCharts();

        $('#treechart-picker').on('changed.bs.select', function () {
            drawCharts();
        });

        $('#scatterplot-picker').on('changed.bs.select', function () {
            drawCharts();
        });

        $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
            const echartId = e.target.dataset.echartId;
            const echartDomElement = document.getElementById(echartId);
            if (echartDomElement) {
                echarts.getInstanceByDom(echartDomElement).resize()
            }
        });
    });
})(jQuery);
