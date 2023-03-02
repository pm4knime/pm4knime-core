(varExplorer = function() {

    let _representation;
    let _value;

    let view = {};

    view.init = function(representation, value) {
        _representation = representation;
        _value = value;

        createUI(representation.data, representation.variants);
    };
    
    view.getComponentValue = () => {
        _value.firstName = document.getElementById("firstName").value;
        _value.lastName = document.getElementById("lastName").value;

        return _value;
    };

    function createUI(data, tracevariants) {
    
    const script = document.createElement('script');
	script.src = 'https://d3js.org/d3.v4.min.js';
	document.getElementsByTagName("head")[0].appendChild(script);
    
    let body = document.getElementsByTagName("body")[0];
    body.innerHTML = `<div class="container-fluid"></div>`;
   
    /*
    var tablestats = document.createElement('div');
    tablestats.innerHTML = 
            `<p>Number Rows: ${data[0]}</p>
             <p>Number Attributes: ${data[1]}</p>`;
             
    for (var i = 2; i <data.length; i++) {
        let val = data[i];
        tablestats.innerHTML += `<p>Attribute ${i-1}: ${val}</p>`;
    }
    
    body.appendChild(tablestats);
    */
    
    
    
    var variants = document.createElement('table');
    
    variants.style.cssText = 'border-spacing: 5px 10px; border: 1px solid black; align: left;';
    
    var totalfrequency = tracevariants.variants.map(function(value) {
  
  		return value.frequency;
		
	}).reduce((a, b) => a + b, 0);
	
	//IDEA TO GENERATE A SEQUENCE OF POLYGONS:
	
	//var svg = document.getElementById("svg");
	
	//var polygon = document.createElementNS("http://www.w3.org/2000/svg", "polygon");
    
    for (var i = 0; i <tracevariants.variants.length; i++) {
        let trace = tracevariants.variants[i].activities;
        let freq = tracevariants.variants[i].frequency;
        let pct = (freq/totalfrequency*100).toFixed(2);
        
        //let tracelen = trace.length;
 
        //the array for the coordinates of polygons
        //var coords = Array();
        /*
        coords[0] = [
        				[5, 10], [5, 55], [75, 55], [100, 32.5], [75, 10]		
        			];
       */
        
        //the "tr" tag to contain the following two td tags
        var newTr = document.createElement('tr'); 
        
        newTr.style.cssText = `padding: 0; margin: 0`;
        
        //the "td" tag to show the statistics(# cases and the proportion of log)
        var newTdStat = document.createElement('td'); 
        
        //the "td" tag to contain the sequence of activities
        var newTdSvg = document.createElement('td'); 
        
        //to assign the style to the td for stats
        newTdStat.style.cssText = `text-align: right;border: 1px solid black; white-space:pre;color: #595959;font-size:14px;`;
        
        //to set the statistic(the frequency and percentage)
        newTdStat.innerHTML += `${freq} Cases
${pct}% Log`;
        
        //to assign the style to the td for svg  
        newTdSvg.style.cssText = `text-align: left; border: 1px solid black; white-space:nowrap; valign:middle;  align:center; padding: 0; margin: 0;`;
        
        //the "div" that contains the name of activity
        var newdiv = document.createElement('div');
        newdiv.setAttribute("xmlns","http://www.w3.org/1999/xhtml");
        
        //to assign the style of newdiv
        newdiv.style.cssText = `width: 60px; height: 30px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap;font-size:15px; border: 1px solid red; margin-top: 1px; margin-left: 1px; padding-top: 3px;`;
        newdiv.innerHTML += `${trace[0]}`;
        
        //the "foreignobject" that will contain the above div 
        var newforeignObject = document.createElementNS("http://www.w3.org/2000/svg",'foreignObject');
        
        //to set the attributes of newforeignObject
        newforeignObject.setAttribute("x", "10");
        newforeignObject.setAttribute("y", "15");
        newforeignObject.setAttribute("width", "65px");
        newforeignObject.setAttribute("height", "35px");
        newforeignObject.style.cssText = `border: 1px solid blue; align-items: center; display: flex;`;
        
        //the "g" that contains "foreignobject" 
        var newg = document.createElementNS("http://www.w3.org/2000/svg", "g");
        newg.style.cssText = `margin-top: 15px; border: 20px solid green`;
        
        //the "polygon" that will contain the polygon object with coordinates
        var newpolygon = document.createElementNS("http://www.w3.org/2000/svg","polygon");
        
        newpolygon.style.cssText = `fill:rgb(255,255,255);stroke:black;stroke-width:1;  border = 1px solid #000000;`;
        
        //var newactivity = document.createTextNode(${trace[0]});
        
        //the "svg" that will contain "polygon"
        var newsvg = document.createElementNS("http://www.w3.org/2000/svg", "svg");  
        
        //to assign the style to the svg
        newsvg.style.cssText = `height: 60px;overflow: hidden; text-overflow: ellipsis; white-space : nowrap;`;
        
        var svg_dymwidth = trace.length * 120;
        
        newsvg.setAttributeNS(null,"width", svg_dymwidth);
        
        //assign the div into the foreignobject
        newforeignObject.appendChild(newdiv);
        
        newsvg.appendChild(newpolygon);
        
        //this loop does do exact the same thing below
        newpolygon.setAttribute("points", "5,10 5,55 75,55 100,32.5 75,10");
        /*
        for (value of coords[0]) {

  			var point = newsvg.createSVGPoint();
  			point.x = value[0];
  			point.y = value[1];
  			newpolygon.points.appendItem(point);
  		
  		}
        */
        newsvg.appendChild(newforeignObject);
        
        
        //idea not insert the coord into array
        //Directly assign the coordinate to the polygon and insert into svg
        var i = 1;
        /*
    	while (i < tracelen) {
    	
    		
        
        	var ithcoord = [
								[5 + 80*i, 10], 
								[30+ 80*i, 32.5],
								[5 + 80*i, 55],
								[75+ 80*i, 55],
								[100 + 80*i, 32.5],
								[75+ 80*i, 10]
							];
			
			var ithpolygon = document.createElementNS("http://www.w3.org/2000/svg","polygon");
			newsvg.appendChild(ithpolygon);
							
			for (value of ithcoord) {

  				var point = newsvg.createSVGPoint();
  				point.x = value[0];
  				point.y = value[1];
  				ithpolygon.points.appendItem(point);
  			
  			}
  			
        	i++;
    	}
    	*/
        
        /*
        if(tracelen > 1){
        
        	for (var i = 1; i < tracelen; i++) {
	
				coords[i] = [
								[5 + 80*i, 10], 
								[30+ 80*i, 32.5],
								[5 + 80*i, 55],
								[75+ 80*i, 55],
								[100 + 80*i, 32.5],
								[75+ 80*i, 10]
							];
					
			}
        	
        }
        */
        
        /*
        here comes the part that insert the polygons and the text divs
        to reduce using loops, I want to use 
        newpolygon.setAttribute("points", "5,10 5,55 75,55 100,32.5 75,10"); like this
        */
        
        newTdSvg.appendChild(newsvg);
        
        newTr.appendChild(newTdStat);
        
        newTr.appendChild(newTdSvg);
        
        variants.appendChild(newTr);
			
    }
    
    body.appendChild(variants);
   	
    //var dummy = document.createElement('div');
    
    //this dummy shows e.g. kind of experimental object
    //dummy.style.cssText = 'border-spacing: 5px 10px; border: 1px solid black; align: left;';
    
    //dummy.innerHTML  += `${totalfrequency}`
    
    //body.appendChild(polygon);
 
}

    return view;
}());