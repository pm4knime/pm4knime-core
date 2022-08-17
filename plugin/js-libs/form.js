(jsgraphviz = function() {

    let _representation;
    let _value;

    let view = {};



    view.init = function(representation, value) {
        _representation = representation;
        _value = value;

        console.log(representation.dotstr);
        console.log(representation);
        visu(representation.dotstr);

    };

    view.getComponentValue = () => {
        _value.firstName = document.getElementById("firstName").value;
        _value.lastName = document.getElementById("lastName").value;
        _value.dot = document.getElementById("dot").value;

        return _value;
    };

    function createUI() {
        let body = document.getElementsByTagName("body")[0];
        body.innerHTML = `<h2>Example JS form</h2>
        <form>
          <p>First name: <input id="firstName" type="text" value="Dick"></p>
          <p>Last name: <input id="lastName" type="text" value="Grayson"></p>
        </form>`;
    }

    function addScript( src ) {
      var s = document.createElement( 'script' );
      s.setAttribute( 'src', src );
      document.body.appendChild( s );
    }

    function visu( src ) {

        //addScript( '/Users/Ralf/Documents/Git/knime/pm4knime-core/pm4knime-core/plugin/js-lib/viz/full.render.js')
        //addScript( '/Users/Ralf/Documents/Git/knime/pm4knime-core/pm4knime-core/plugin/js-lib/viz/viz.js' )



        var viz = new Viz();

        viz.renderSVGElement(src)
          .then(function(element) {
            document.body.appendChild(element);
          })
          .catch(error => {
            // Create a new Viz instance (@see Caveats page for more info)
            viz = new Viz();

            // Possibly display the error
            console.error(error);
          });

    }

    return view;
}());
