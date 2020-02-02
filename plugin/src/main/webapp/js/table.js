/* global jQuery, math, view */
(function ($) {

    $.fn.extend({
        /**
         * Renders the metrics table using datatables.
         *
         * @param {Array} supportedMetrics - the metrics supported for this table
         */
        renderMetricsTable: function (supportedMetrics) {
            const table = $(this).DataTable({
                ajax: function (_data, callback, _settings) {
                    view.getMetricsJSON(res => {
                        callback({data: JSON.parse(res.responseJSON).rows});
                    });
                },
                columns: [
                    {
                        name: '',
                        orderable: false,
                        data: null,
                        width: '10px',
                        title: '',
                        defaultContent: "<span class='details-control'/>"
                    },
                    {
                        data: 'name',
                        title: 'Name',
                        defaultContent: '',
                        className: 'hideable'
                    },
                    ...supportedMetrics
                        .filter(({scopes}) => scopes.includes('CLASS'))
                        .map(({id, displayName}) => ({
                                data: 'metricsDisplay.' + id,
                                title: displayName,
                            defaultContent: '',
                            className: 'hideable'
                            })
                        )
                ],
                order: [[1, 'asc']],
                responsive: {
                    details: {
                        type: 'column'
                    }
                },
                colReorder: {
                    fixedColumnsLeft: 1
                },
                buttons: {
                    buttons: [{
                        extend: 'colvis',
                        columns: '.hideable'
                    }]
                },
                initComplete: function (settings, _json) {
                    const api = new $.fn.dataTable.Api(settings);
                    api.buttons().container().appendTo($('#column-dropdown'));
                }
            });
        }
    });
})(jQuery);
