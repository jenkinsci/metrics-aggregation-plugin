/* global jQuery, math */
(function ($) {

    $.fn.extend({
        /**
         * Adds a filter to a dataTable
         *
         * @param {Array} supportedMetrics - the metrics supported for this table
         * @param table - the jQuery reference for the datatable to use
         */
        filterTable: function (supportedMetrics, table) {
            const emptyData = Object.fromEntries(supportedMetrics.map(({id}) => [id, 0]));
            let filterExpression = math.compile('true');

            /**
             * TODO source
             * @param func
             * @param wait
             * @returns {Function}
             */
            function debounce(func, wait) {
                let timeoutId;
                return function () {
                    clearTimeout(timeoutId);

                    const context = this, args = arguments;
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
                    $(this).removeClass('is-invalid');
                    return filterExpression.evaluate(data)
                } catch (e) {
                    $(this).next('.invalid-feedback').text(e.message);
                    $(this).addClass('is-invalid');
                    if (rethrow) {
                        throw e;
                    }
                    return false;
                }
            }

            $.fn.dataTable.ext.search.push(
                function (_settings, _searchData, _index, rowData, _counter) {
                    return filterData(rowData.metricsRaw);
                }
            );

            var redrawTableDebounced = debounce(function () {
                table.DataTable().draw();
            }, 250);

            $(this).on('input', function () {
                try {
                    $(this).removeClass('is-invalid');
                    const filterString = $(this).val() || 'true';
                    filterExpression = math.compile(filterString);
                } catch (e) {
                    $(this).next('.invalid-feedback').text(e.message);
                    $(this).addClass('is-invalid');
                }

                // try filtering the data to detect any errors in the input
                try {
                    filterData(emptyData, true);
                    redrawTableDebounced();
                } catch {
                    // ignored, already caught in filter data
                }
            });
        }
    });
})(jQuery);
