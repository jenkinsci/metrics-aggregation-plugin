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
            var chart = echarts.init($(this)[0], 'light');
            var options = {
                title: {
                    text: metricName + ' tree map',
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
                        upperLabel: {
                            show: true,
                            height: 22
                        },
                        itemStyle: {
                            color: "#eee",
                            borderColorSaturation: 0.4,
                            borderWidth: 2,
                            gapWidth: 2
                        },
                        levels: [
                            {
                                upperLabel: {
                                    show: false
                                }
                            },
                            {
                                // echarts light theme color palette
                                color: ["#37A2DA", "#32C5E9", "#67E0E3", "#9FE6B8", "#FFDB5C", "#ff9f7f", "#fb7293", "#E062AE", "#E690D1", "#e7bcf3", "#9d96f5", "#8378EA", "#96BFFF"],
                                itemStyle: {
                                    borderWidth: 4,
                                    gapWidth: 3
                                }
                            },
                            {
                                colorSaturation: [0.35, 0.6],
                                itemStyle: {
                                    borderColorSaturation: 0.7
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
