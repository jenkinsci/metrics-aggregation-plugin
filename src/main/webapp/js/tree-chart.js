/* global jQuery, echarts */
(function ($) {
    $.fn.extend({
        /**
         * Renders a tree chart using ECharts.
         *
         * @param {String} treeModel - the tree chart data model
         * @param {String} metricName - the name of the metric, used e.g. as title for the graph
         */
        renderTreeChart: function (treeModel, metricName) {
            var chart = echarts.init($(this)[0]);
            var options = {
                title: {
                    text: metricName,
                    left: 'center'
                },

                tooltip: {
                    formatter: function (info) {
                        // shift to remove the first level (name of the metric) from the path
                        info.treePathInfo.shift();

                        var fileName = info.treePathInfo
                            .map(i => i.name)
                            .join('.');

                        return '<b>' + echarts.format.encodeHTML(fileName) + '</b><br/>'
                            + metricName + ': ' + echarts.format.addCommas(info.value);
                    }
                },

                series: [
                    {
                        name: metricName,
                        type: 'treemap',
                        label: {
                            show: true,
                            formatter: '{b}'
                        },
                        itemStyle: {
                            normal: {
                                borderColor: '#aaa',
                                gapWidth: 4
                            }
                        },
                        levels: [
                            {
                                // top level empty
                            },
                            {
                                itemStyle: {
                                    normal: {
                                        borderColor: '#888',
                                        borderWidth: 5,
                                        gapWidth: 5
                                    }
                                }
                            }
                        ],
                        data: [treeModel]
                    }
                ]
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
