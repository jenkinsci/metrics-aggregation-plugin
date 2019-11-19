/* global jQuery, Prototype */
(function ($) {
    /**
     * Solves Bootstrap and Prototype.js conflict.
     *
     * @link http://jsfiddle.net/dgervalle/hhBc6/
     * @link http://www.softec.lu/site/DevelopersCorner/BootstrapPrototypeConflict
     */
    jQuery.noConflict();
    if (Prototype.BrowserFeatures.ElementExtensions) {
        const disablePrototypeJS = function (method, pluginsToDisable) {
            const handler = function (event) {
                event.target[method] = undefined;
                setTimeout(function () {
                    delete event.target[method];
                }, 0);
            };
            pluginsToDisable.each(function (plugin) {
                jQuery(window).on(method + '.bs.' + plugin, handler);
            });
        };
        const pluginsToDisable = ['collapse', 'dropdown', 'modal', 'tooltip', 'popover', 'tab'];
        disablePrototypeJS('show', pluginsToDisable);
        disablePrototypeJS('hide', pluginsToDisable);
    }

    /**
     * Function to get an object in its browser-default implementation.
     *
     * @param constructor the object to reset
     * @returns {*} the freshly-reset object
     *
     * @link https://stackoverflow.com/questions/13990187/create-a-reset-of-javascript-array-prototype-when-array-prototype-has-been-modif
     */
    var reset = function reset(constructor) {
        if (!(constructor.name in reset)) {
            var iframe = document.createElement('iframe');
            iframe.src = 'about:blank';
            document.body.appendChild(iframe);
            reset[constructor.name] = iframe.contentWindow[constructor.name];
            document.body.removeChild(iframe);
        }
        return reset[constructor.name];
    };

    /**
     * Check if a prototype function is no longer a browser default implementation, but a custom one.
     * Beware! This will not work if two prototypes are swapped like e.g. `Array.prototype.map = String.prototype.sort`
     *
     * @param func the prototype function to test
     * @returns {boolean} true, if the prototype is a custom implementation, false otherwise
     *
     * @link https://stackoverflow.com/questions/574584/javascript-check-if-method-prototype-has-been-changed
     */
    function customPrototype(func) {
        return !/^\s*function[^{]+{\s*\[native code\]\s*}\s*$/.test(func);
    }

    /**
     * Prototype.js v1.7.0 is deviating from the ES5 spec for Array methods. Since upgrading the library in Jenkins
     * seems not to be possible, this resets the conflicting Array.prototype.map to the browser implementation.
     *
     * Needed for math.js.
     *
     * @link https://github.com/prototypejs/prototype/commit/84c7f29e36f8149da10b11fe934e4a01a9371981
     * @link https://github.com/jenkinsci/jenkins/pull/3277
     */
    if (customPrototype(Array.prototype.map)) {
        // eslint-disable-next-line no-extend-native
        Array.prototype.map = reset(Array).prototype.map;
    }

    /**
     * The file hudson-behavior.js is resetting the String.prototype.trim function. Reset it to the default.
     *
     * Needed for math.js.
     *
     * @link https://github.com/jenkinsci/jenkins/commit/617944580b634678f40f861fc4949c7f44bcbf9a
     */
    if (customPrototype(String.prototype.trim)) {
        // eslint-disable-next-line no-extend-native
        String.prototype.trim = reset(String).prototype.trim;
    }
})(jQuery);
