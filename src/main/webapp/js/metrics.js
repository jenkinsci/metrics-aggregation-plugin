/* global jQuery, view, echarts, metrics, supportedMetrics, math */
(function ($) {
    function max(metricId) {
        return Math.max(...metrics.map(m => m.metrics[metricId]).filter(Boolean))
    }

    var childTables = {};
    var visibleColumns = [];
    var filterExpression = math.compile('true');

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
     * @param data the data of a row to filter
     * @param {boolean} [rethrow] whether to rethrow any caught exceptions
     * @returns {boolean|*} the result for the filter expression
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
        function (settings, rawData, dataIndex) {
            var data = {
                ATFD: rawData[3],
                CLASS_FAN_OUT: rawData[4],
                LOC: rawData[5],
                NCSS: rawData[6],
                NOAM: rawData[7],
                NOPA: rawData[8],
                TCC: rawData[9],
                WMC: rawData[10],
                WOC: rawData[11],
                ISSUES: rawData[12]
            };

            return filterData(data);
        }
    );

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
        var table = $('#lines-of-code').DataTable({
            data: metrics,
            columns: [
                {
                    className: '',
                    ordering: false,
                    data: null,
                    width: '10px',
                    defaultContent: "<div class='details-control'/>"
                },
                {
                    data: 'className',
                    name: 'className',
                    defaultContent: ''
                },
                ...supportedMetrics.map(metric => ({
                        data: 'metrics.' + metric.id,
                        name: 'metrics.' + metric.id,
                        defaultContent: '',
                        render: $.fn.dataTable.render.number(',', '.', 2)
                    })
                )
            ],
            columnDefs: [],
            responsive: {
                details: false
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

            var emptyData = {
                ATFD: 0, CLASS_FAN_OUT: 0, LOC: 0,
                NCSS: 0, NOAM: 0, NOPA: 0, TCC: 0,
                WMC: 0, WOC: 0, ISSUES: 0
            };

            // try filtering the data to detect any errors in the input
            try {
                filterData(emptyData, true);
                redrawTableDebounced();
            } catch {
                // ignored, already caught in filter data
            }
        });

        // Add event listener for opening and closing details
        $('#lines-of-code tbody').on('click', 'div.details-control', function () {
            var tr = $(this).closest('tr');
            var row = table.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');

                delete childTables[row.index()];
            } else {
                // Open this row
                var child = '<table id="child_' + row.index() + '" class="ml-5">' +
                    '<thead>' +
                    '<th>Method</th>' +
                    '<th class="hideable">Lines of Code</th>' +
                    '<th class="hideable">Non-comment</th>' +
                    '</thead>' +
                    '</table>';

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
        });

        // toggle column visibility
        function changeVisibility() {
            if (this.visible() && visibleColumns.indexOf(this.name()) <= -1) {
                this.visible(false);
            } else if (!this.visible() && visibleColumns.indexOf(this.name()) > -1) {
                this.visible(true);
            }
        }

        $('#column-picker').on('changed.bs.select', function (e, _clickedIndex, _isSelected, _previousValue) {
            visibleColumns = $(e.target).val();

            // change visibilities in main table
            table.columns('.hideable').every(changeVisibility);

            // change visibilities in child tables
            Object.values(childTables)
                .forEach(childTable => childTable.columns('.hideable').every(changeVisibility));
        });

        $('#lines-of-code tbody').on('mouseenter', 'td', function () {
            var column = table.column($(this));
            var metricId = column.name().replace('metrics.', '');
            var maxValue = max(metricId);
            table.cells(undefined, column.index()).every(function () {
                var lightness = (1 - this.data() / maxValue) * 60 + 35;
                //$(this.node()).addClass('warning');
                $(this.node()).css('background', 'hsl(200, 100%, ' + lightness + '%)');
            });
        });

        $('#lines-of-code tbody').on('mouseleave', 'td', function () {
            var column = table.column($(this));
            table.cells(undefined, column.index()).every(function () {
                $(this.node()).css('background', '');
            });
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
