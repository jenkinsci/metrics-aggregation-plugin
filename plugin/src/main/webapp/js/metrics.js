/* global jQuery, view */
(function ($) {
    $(document).ready(function () {

        /* ------------------------------------------------------------------------------
                                            table
         ------------------------------------------------------------------------------ */

        view.getSupportedMetrics(function (res) {
            $('#metrics-table').renderMetricsTable(res.responseJSON);
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
                $('#histogram').renderHistogram(res.responseJSON, metricId);
            })
        }

        drawCharts();

        $('#treechart-picker').on('changed.bs.select', function (e) {
            if (previousValue && previousValue !== $(e.target).val()) {
                drawCharts();
            }
        });
    });
})(jQuery);
