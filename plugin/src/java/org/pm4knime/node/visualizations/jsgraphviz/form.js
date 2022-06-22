jsform = (function () {

    var _representation = undefined;
    var _value = undefined;

    var view = {};

    view.init = function (representation, value) {
        _representation = representation;
        _value = value;

        console.log(representation.dotstr);
        console.log(representation);
        visu(representation.dotstr);
    };

    view.getComponentValue = function () {
        _value.firstName = document.getElementById("firstName").value;
        _value.lastName = document.getElementById("lastName").value;
        _value.dot = document.getElementById("dot").value;

        return _value;
    };

    function createUI() {
        var body = document.getElementsByTagName("body")[0];
        body.innerHTML = "<h2>Example JS form</h2>\n        <form>\n          <p>First name: <input id=\"firstName\" type=\"text\" value=\"Dick\"></p>\n          <p>Last name: <input id=\"lastName\" type=\"text\" value=\"Grayson\"></p>\n        </form>";
    }

    function addScript(src) {
        var s = document.createElement('script');
        s.setAttribute('src', src);
        document.body.appendChild(s);
    }

    function visu(src) {

        addScript( '/Users/kourani/git/pm4knime-core/plugin/js-lib/viz/full.render.js')
        addScript( '/Users/kourani/git/pm4knime-core/plugin/js-lib/viz/viz.js' )
        var viz = new Viz();
        viz.renderSVGElement(src).then(function (element) {
            document.body.appendChild(element);
        })["catch"](function (error) {
            // Create a new Viz instance (@see Caveats page for more info)
            viz = new Viz();

            // Possibly display the error
            console.error(error);
        });
    }

    return view;
})();






