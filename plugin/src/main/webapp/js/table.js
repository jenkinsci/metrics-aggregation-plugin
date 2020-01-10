/* global jQuery, math, metricsMaxima, view */
(function ($) {

    $.fn.extend({
        /**
         * Renders the metrics table using datatables.
         *
         * @param {Array} supportedMetrics - the metrics supported for this table
         */
        renderMetricsTable: function (supportedMetrics) {
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
             * Extend the dataTables api to provide a method for retrieving the name of one or multiple column(s).
             *
             * Source: https://datatables.net/forums/discussion/44885
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
                const childDiv = $('<div />');
                const row = api.row(rowIdx);
                const hiddenRows = $('<table />');
                hiddenRows.DataTable({
                    data: [[]], //necessary so the defaultContent can be used for the values of the columns below
                    columns: [
                        {
                            title: 'Package',
                            defaultContent: row.data().packageName,
                            data: null
                        },
                        ...columns
                            .filter(({hidden}) => hidden)
                            .map(column => ({
                                title: column.title,
                                defaultContent: column.data,
                                data: null
                            }))
                    ]
                });
                childDiv.append(hiddenRows);

                if (row.data().children && row.data().children.length > 0) {
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
                            ...supportedMetrics
                                .filter(({scopes}) => scopes.includes('METHOD'))
                                .map(({id, displayName}) => ({
                                        data: 'metricsDisplay.' + id,
                                        name: 'metricsRaw.' + id,
                                        title: displayName,
                                        defaultContent: ''
                                    })
                                )
                        ]
                    });
                    childDiv.append(methodDetails);
                }

                return childDiv;
            }

            var table = $(this).DataTable({
                ajax: function (_data, callback, _settings) {
                    view.getMetricsJSON(res => {
                        callback({data: JSON.parse(res.responseJSON)});
                    });
                },
                columns: [
                    {
                        name: '',
                        orderable: false,
                        data: null,
                        width: '10px',
                        title: '',
                        defaultContent: "<div class='details-control'/>"
                    },
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
        }
    });
})(jQuery);
