/* global jQuery, math, metricsMaxima, view */
(function ($) {

    $.fn.extend({
        /**
         * Renders the metrics table using datatables.
         *
         * @param {Array} supportedMetrics - the metrics supported for this table
         */
        renderMetricsTable: function (supportedMetrics) {

            var table = $(this).DataTable({
                ajax: function (_data, callback, _settings) {
                    view.getMetricsJSON(res => {
                        callback({data: JSON.parse(res.responseJSON)});
                    });
                },
                columns: [
                    {
                        data: 'className',
                        name: 'className',
                        title: 'Class',
                        defaultContent: ''
                    },
                    ...supportedMetrics
                        .filter(({scopes}) => scopes.includes('CLASS'))
                        .map(({id, displayName}) => ({
                                data: 'metricsDisplay.' + id,
                                name: 'metricsRaw.' + id,
                                title: displayName,
                                defaultContent: ''
                            })
                        )
                ],
                order: [[1, 'asc']],
                responsive: {},
                colReorder: {
                    fixedColumnsLeft: 1
                },
                buttons: {
                    buttons: [{
                        extend: 'columnsToggle',
                        columns: '.hideable'
                    }],
                    dom: {
                        container: {
                            className: 'dropdown-menu'
                        },
                        button: {
                            className: 'dropdown-item',
                            tag: 'button',
                            active: 'selected'
                        },
                        buttonLiner: {
                            tag: null
                        }
                    }
                }
            });


            /*
            // Event listener for coloring the table columns
            $('#metrics-table tbody')
                .on('mouseenter', 'td', function () {
                    var column = table.column($(this));
                    var metricId = (column.name() || '').replace('metrics.', '');
                    var maxValue = metricsMaxima[metricId];
                    table.cells(':visible', column.index()).every(function () {
                        var lightness = (1 - this.data() / maxValue) * 60 + 35;
                        //$(this.node()).addClass('warning');
                        $(this.node()).css('background', 'hsl(200, 100%, ' + lightness + '%)');
                    });
                })
                .on('mouseleave', 'td', function () {
                    var column = table.column($(this));
                    table.cells(':visible', column.index()).every(function () {
                        $(this.node()).css('background', '');
                    });
                });
                */
        }
    });
})(jQuery);
