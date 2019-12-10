/* global jQuery, view, echarts, metrics, supportedMetrics, metricsMaxima, math */
(function ($) {
    var childTables = {};
    var visibleColumns = [];
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
            return filterData(rowData.metrics);
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


    $(document).ready(function () {
        var table = $('#metrics-table').DataTable({
            dom: 'Blfrtip',
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
                        data: 'metrics.' + id,
                        name: 'metrics.' + id,
                        defaultContent: '',
                        render: $.fn.dataTable.render.number(',', '.', 2)
                    })
                )
            ],
            columnDefs: [],
            responsive: {
                details: false
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
        window.table = table

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

        // Add event listener for opening and closing details
        $('#metrics-table tbody')
            .on('click', 'div.details-control', function () {
                var tr = $(this).closest('tr');
                var row = table.row(tr);

                if (row.child.isShown()) {
                    // This row is already open - close it
                    row.child.hide();
                    tr.removeClass('shown');

                    delete childTables[row.index()];
                } else {
                    // Open this row
                    var child = '<div>' +
                        '<div>' +
                        '<b>Package:</b> ' + row.data().packageName +
                        '</div>' +
                        '<table id="child_' + row.index() + '" class="ml-5">' +
                        '<thead>' +
                        '<th>Method</th>' +
                        '<th class="hideable">Lines of Code</th>' +
                        '<th class="hideable">Non-comment</th>' +
                        '</thead>' +
                        '</table>' +
                        '</div>';

                    row.child(child).show();
                    tr.addClass('shown');

                    childTables[row.index()] = $('#child_' + row.index()).DataTable({
                        info: false,
                        paging: false,
                        searching: false,
                        data: row.data().children,
                        columns: [
                            {
                                data: 'methodName',
                                name: 'methodName'
                            },
                            {
                                data: 'metrics.LOC',
                                name: 'metrics.LOC',
                                visible: visibleColumns.indexOf('metrics.LOC') <= -1
                            },
                            {
                                data: 'metrics.NCSS',
                                name: 'metrics.NCSS',
                                visible: visibleColumns.indexOf('metrics.NCSS') <= -1
                            }
                        ]
                    });
                }
            })
            .on('mouseenter', 'td', function () {
                var column = table.column($(this));
                var metricId = column.name().replace('metrics.', '');
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

        // toggle column visibility
        function changeVisibility() {
            if (this.visible() && visibleColumns.indexOf(this.name()) <= -1) {
                this.visible(false);
            } else if (!this.visible() && visibleColumns.indexOf(this.name()) > -1) {
                this.visible(true);
            }
        }

        $('#column-picker').on('changed.bs.select', function (e) {
            visibleColumns = $(e.target).val();

            // change visibilities in main table
            table.columns('.hideable').every(changeVisibility);

            // change visibilities in child tables
            Object.values(childTables)
                .forEach(childTable => childTable.columns('.hideable').every(changeVisibility));
        });

        /* ------------------------------------------------------------------------------
                                            tree chart
         ------------------------------------------------------------------------------ */

        function drawTreeChart() {
            var metric = $('#treechart-picker').val();
            var metricName = $('#treechart-picker :selected').text();

            view.getMetricsTree(metric, function (res) {
                $('#treechart').renderTreeChart(res.responseJSON, metricName);
            });

            view.getHistogram(metric, function (res) {
                $('#histogram').renderHistogram(res.responseJSON, metricName);
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
