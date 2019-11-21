/* global jQuery, echarts */
(function ($) {
    $.fn.extend({
        /**
         * Renders a tree chart using ECharts.
         *
         * @param {String} model - the chart model
         */
        renderTreeChart: function (model, metricTitle) {
            var treeModel = JSON.parse(model);
            var chart = echarts.init($(this)[0]);
            var options = {
                title: {
                    text: metricTitle,
                    left: 'center'
                },

                tooltip: {
                    formatter: function (info) {
                        var fileName = info.treePathInfo
                            .map(i => i.name)
                            .join('.');

                        return echarts.format.encodeHTML(fileName) + ' ' + echarts.format.addCommas(info.value);
                    }
                },

                series: [
                    {
                        name: metricTitle,
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
            chart.on('click', function (params) {
                window.location.assign(params.name);
            });
            $(window).on('resize', function () {
                chart.resize();
            });
            $(this).data('chart', chart);
        }
    });
})(jQuery);
