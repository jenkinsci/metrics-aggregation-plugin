/* global jQuery, echarts */
(function ($) {
    $.fn.extend({
        /**
         * Renders a tree chart using ECharts.
         *
         * @param {String} model - the chart model
         */
        renderTreeChart: function (model) {
            var treeModel = JSON.parse(model);
            var chart = echarts.init($(this)[0]);
            var options = {
                title: {
                    text: 'Lines of Code',
                    left: 'center'
                },

                tooltip: {
                    formatter: function (info) {
                        var treePathInfo = info.treePathInfo;
                        var treePath = [];

                        for (var i = 1; i < treePathInfo.length; i++) {
                            treePath.push(treePathInfo[i].name);
                        }

                        return '<b>' + echarts.format.encodeHTML(treePath.join('.')) + '</b><br/>' +
                            echarts.format.addCommas(info.value) + ' Lines';
                    }
                },

                series: [
                    {
                        name: 'Lines of Code',
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
