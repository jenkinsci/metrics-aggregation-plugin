/* global jQuery, view, echarts, metrics, supportedMetrics, metricsMaxima, math */
(function ($) {
    var filterExpression = math.compile('true');

    /**
     * TODO source
     * @param func
     * @param wait
     * @returns {Function}
     */
    function debounce(func, wait) {
        var timeoutId;
        return function () {
            clearTimeout(timeoutId);

            var context = this, args = arguments;
            timeoutId = setTimeout(function () {
                timeoutId = null;
                func.apply(context, args);
            }, wait);
        };
    }

    /**
     * Filter the data of a row with the user defined filter.
     *
     * @param data - the data of a row to filter
     * @param {boolean} [rethrow] - whether to rethrow any caught exceptions
     * @returns {boolean|*} - the result for the filter expression
     */
    function filterData(data, rethrow) {
        try {
            $('#table-filter').removeClass('is-invalid');
            return filterExpression.evaluate(data)
        } catch (e) {
            $('#table-filter ~ .invalid-feedback').text(e.message);
            $('#table-filter').addClass('is-invalid');
            if (rethrow) {
                throw e;
            }
            return true;
        }
    }

    $.fn.dataTable.ext.search.push(
        function (_settings, _searchData, _index, rowData, _counter) {
            return filterData(rowData.metricsRaw);
        }
    );

    /**
     * TODO source (datatables.net forum)
     */
    $.fn.dataTable.Api.registerPlural('columns().names()', 'column().name()', function (setter) {
        return this.iterator('column', function (settings, column) {
            var col = settings.aoColumns[column];

            if (setter !== undefined) {
                col.sName = setter;
                return this;
            } else {
                return col.sName;
            }
        }, 1);
    });

    function responsiveDetailsRenderer(api, rowIdx, columns) {
        const hiddenRows = $('<table />');
        hiddenRows.DataTable({
            columns: [
                {
                    data: 'packageName',
                    name: 'packageName',
                    title: 'Package',
                    defaultContent: ''
                },
                ...columns
                    .filter(({hidden}) => hidden)
                    .map(column => ({
                        defaultContent: column.data,
                        data: null,
                        title: column.title
                    }))
            ]
        });

        const row = api.row(rowIdx);
        const methodDetails = $('<table />');
        methodDetails.DataTable({
            info: false,
            paging: false,
            searching: false,
            data: row.data().children,
            columns: [
                {
                    data: 'methodName',
                    name: 'methodName',
                    title: 'Method'
                },
                {
                    data: 'beginLine',
                    name: 'beginLine',
                    title: 'Line'
                },
                {
                    data: 'metricsDisplay.LOC',
                    name: 'metricsRaw.LOC'
                },
                {
                    data: 'metricsDisplay.NCSS',
                    name: 'metricsRaw.NCSS'
                }
            ]
        });

        return $('<div />')
            .append(hiddenRows)
            .append(methodDetails);
    }

    $(document).ready(function () {
        var table = $('#metrics-table').DataTable({
            data: metrics,
            columns: [
                {
                    name: '',
                    orderable: false,
                    data: null,
                    width: '10px',
                    defaultContent: "<div class='details-control'/>"
                },
                {
                    data: 'className',
                    name: 'className',
                    defaultContent: ''
                },
                ...supportedMetrics.map(({id}) => ({
                        data: 'metricsDisplay.' + id,
                        name: 'metricsRaw.' + id,
                        defaultContent: ''
                    })
                )
            ],
            order: [[1, 'asc']],
            responsive: {
                details: {
                    type: 'column',
                    renderer: responsiveDetailsRenderer
                }
            },
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

        table.buttons().container().appendTo($('#column-dropdown'));

        $('#column-dropdown').on('hide.bs.dropdown', function (event) {
            if (event.clickEvent && $.contains(event.target, event.clickEvent.target)) {
                // if a button inside the list has been clicked, do not close the dropdown
                event.preventDefault();
                return false;
            }
        });

        var redrawTableDebounced = debounce(function () {
            table.draw();
        }, 250);

        $('#table-filter').on('input', function () {
            try {
                $('#table-filter').removeClass('is-invalid');
                var filterString = $('#table-filter').val() || 'true';
                filterExpression = math.compile(filterString);
            } catch (e) {
                $('#table-filter ~ .invalid-feedback').text(e.message);
                $('#table-filter').addClass('is-invalid');
            }

            var emptyData = Object.fromEntries(supportedMetrics.map(({id}) => [id, 0]));

            // try filtering the data to detect any errors in the input
            try {
                filterData(emptyData, true);
                redrawTableDebounced();
            } catch {
                // ignored, already caught in filter data
            }
        });

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

        /* ------------------------------------------------------------------------------
                                            tree chart
         ------------------------------------------------------------------------------ */

        function drawTreeChart() {
            var metric = $('#treechart-picker').val();
            var metricId = $('#treechart-picker :selected').text();

            view.getMetricsTree(metric, function (res) {
                $('#treechart').renderTreeChart(res.responseJSON, metricId);
            });

            view.getHistogram(metric, function (res) {
                $('#histogram').renderHistogram(res.responseJSON, metricId);
            })
        }

        drawTreeChart();

        $('#treechart-picker').on('changed.bs.select', function (e, _clickedIndex, _isSelected, previousValue) {
            if (previousValue && previousValue !== $(e.target).val()) {
                drawTreeChart();
            }
        });
    });
})(jQuery);
