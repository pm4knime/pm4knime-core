(jsgraphviz = function() {

    let _representation;
    let _value;
    let _svg;

    let view = {};



    view.init = function(representation, value) {
        _representation = representation;
        _value = value;

        console.log(representation.dotstr);
        console.log(representation);
        vis = visu(representation.dotstr);
        
    };

    view.getComponentValue = () => {
        _value.dot = document.getElementById("dot").value;

        return _value;
    };
    
    view.getSVG = () => {
        return (new XMLSerializer()).serializeToString(_svg);;
    };
    
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
               
           _svg = element;
           
           var exportA = document.createElement('a');
           exportA.innerHTML = `<button type="button">Export</button>`;
           exportA.href = viz.generate_url(element);
           exportA.download = "js-view.svg";
           
           

           var zoomIn = document.createElement('a');
           zoomIn.innerHTML = `<button type="button">Zoom in</button>`;
           zoomIn.addEventListener( 'click', function(){
               element.setAttribute("max-height", "none"); 
               element.setAttribute("max-width", "none"); 
               var width = element.clientWidth*1.1; 
               var height = element.clientHeight*1.1; 
               element.setAttribute("width", width + "px"); 
               element.setAttribute("height", height + "px"); 
               
           });
           
           var zoomOut = document.createElement('a');
           zoomOut.innerHTML = `<button type="button">Zoom out</button>`;
           zoomOut.addEventListener( 'click', function(){
               element.setAttribute("max-height", "none"); 
               element.setAttribute("max-width", "none"); 
               var width = element.clientWidth*0.9; 
               var height = element.clientHeight*0.9; 
               element.setAttribute("width", width + "px"); 
               element.setAttribute("height", height + "px"); 
           });
           
           var parent1 = document.createElement("div");
           parent1.style.top = "0px";
           parent1.style.left = "0px";
           parent1.style.position = "fixed";
           parent1.style.border = "groove #e6e6e6";
           parent1.appendChild(exportA);
           parent1.appendChild(zoomIn);
           parent1.appendChild(zoomOut);
           document.body.appendChild(parent1);     
                     
           var parent2 = document.createElement("div");
           parent2.style.top = "50px";
           parent2.style.left = "0px";
           parent2.style.position = "absolute";
           parent2.appendChild(element);              
           document.body.appendChild(parent2); 
            
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
