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
	
	
	//var polygon = document.createElementNS("http://www.w3.org/2000/svg", "polygon");

	/*
	
	Now, to assign an unique color to each activity!
	by using tracevariants.activities
	
	*/
	
	//idea to generate the random colors
	var colors = Array(tracevariants.activities.length).fill().map((_, i) => (i+Math.floor(Math.random()*16777215)));
	
	//to determine the brightness of each color in colors
	//then assign the font color(either black or white)
	var brightnesses = colors.map(x => 
            ((x >> 16)&0xff)*0.2126 + 
            ((x >> 8)&0xff)*0.7152 + 
            ((x >> 0)&0xff)*0.0722);
    
    brightnesses = (brightnesses > 125) ? "black" : "white";
    
    //color codes of the generated color
    var colorcodes = colors.map(x => 
            "#" + x.toString(16)
            );

   	//beasts.indexOf('bison')    
    for (var i = 0; i <tracevariants.variants.length; i++) {
        let trace = tracevariants.variants[i].activities;
        let freq = tracevariants.variants[i].frequency;
        let pct = (freq/totalfrequency*100).toFixed(2);
        
        let tracelen = trace.length;
 
        var newTr = document.createElement('tr'); 
        newTr.style.cssText = `padding: 0; margin: 0`;
        
        var newTdStat = document.createElement('td'); 
        newTdStat.style.cssText = `text-align: right;border: 1px solid black; white-space:pre;color: #595959;font-size:14px;`;
        newTdStat.innerHTML += ` ${freq} Cases <br> ${pct}% Log `;
        newTr.appendChild(newTdStat);
        
        var newTdSvg = document.createElement('td'); 
        newTdSvg.style.cssText = `text-align: left; border: 1px solid black; white-space:nowrap; valign:middle;  align:center; padding: 0; margin: 0;`;
        
        //the "svg" that will contain "polygon"
        var newsvg = document.createElementNS("http://www.w3.org/2000/svg", "svg");  
        
        //to assign the style to the svg
        newsvg.style.cssText = `height: 60px;overflow: hidden; text-overflow: ellipsis; white-space : nowrap;`;
        
        var svg_dymwidth = trace.length * 180;
        
        newsvg.setAttributeNS(null,"width", svg_dymwidth);
        
        var firstpolygon = document.createElementNS("http://www.w3.org/2000/svg","polygon");
        
        firstpolygon.style.cssText = `stroke:black;stroke-width:1;  border = 1px solid #000000;`;

        firstpolygon.setAttribute("points", "5,10 " + "5,55 " + "145,55 " + "160,32.5 " + "145,10");
        
        firstpolygon.style.fill = colorcodes[tracevariants.activities.indexOf(trace[0])];
        
        newsvg.appendChild(firstpolygon);
        
        var firstdiv = document.createElement('div');
        firstdiv.setAttribute("xmlns","http://www.w3.org/1999/xhtml");
        
        firstdiv.style.cssText = `width: 130px; height: 30px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap;font-size:15px; margin-top: 1px; margin-left: 1px; padding-top: 5px; text-align: center`;
        firstdiv.innerHTML += `${trace[0]}`;
        
        firstdiv.style.color = brightnesses[tracevariants.activities.indexOf(trace[0])];
        var firstforeignObject = document.createElementNS("http://www.w3.org/2000/svg",'foreignObject');
        
        firstforeignObject.setAttribute("x", 10);
        firstforeignObject.setAttribute("y", 15);
        firstforeignObject.setAttribute("width", "135px");
        firstforeignObject.setAttribute("height", "35px");
        
        firstforeignObject.style.cssText = ` align-items: center; display: flex;`;
        
        firstforeignObject.appendChild(firstdiv);
        
        var firstg = document.createElementNS("http://www.w3.org/2000/svg", "g");
        firstg.style.cssText = `margin-top: 15px; border: 20px solid green`;

        firstg.appendChild(firstforeignObject);
        newsvg.appendChild(firstg);
            
     var j = 1;
     
     while (j < trace.length) {
        //the "polygon" that will contain the polygon object with coordinates
        var newpolygon = document.createElementNS("http://www.w3.org/2000/svg","polygon");
        
        newpolygon.style.cssText = `fill:rgb(255,255,255);stroke:black;stroke-width:1;border = 1px solid #000000;`;
   		
        newpolygon.setAttribute("points", (160*j - 10)   + ",10 "   + 
        								  (145 + 160*j )  + ",10 "   +
        								  (160 + 160*j ) + ",32.5 " +
        								  (145 + 160*j )  + ",55 "   + 
        								  (160*j - 10)   + ",55 "   + 
        								  (160*j + 5)  + ", 32.5 " );
        								  
       	newpolygon.style.fill = colorcodes[tracevariants.activities.indexOf(trace[j])];

        newsvg.appendChild(newpolygon);

        //the "div" that contains the name of activity
        var newdiv = document.createElement('div');
        newdiv.setAttribute("xmlns","http://www.w3.org/1999/xhtml");
        
        //to assign the style of newdiv
        newdiv.style.cssText = `width: 130px; height: 30px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap;font-size:15px; margin-top: 1px; margin-left: 1px; padding-top: 5px; text-align: center`;
        newdiv.innerHTML += `${trace[j]}`;
		newdiv.style.color = brightnesses[tracevariants.activities.indexOf(trace[j])];
        
        //the "foreignobject" that will contain the above div 
        var newforeignObject = document.createElementNS("http://www.w3.org/2000/svg",'foreignObject');
        
        //to set the attributes of newforeignObject
        newforeignObject.setAttribute("x", 10 + 160*j);
        newforeignObject.setAttribute("y", 15);
        newforeignObject.setAttribute("width", "135px");
        newforeignObject.setAttribute("height", "35px");
        newforeignObject.style.cssText = ` align-items: center; display: flex;`;

        newforeignObject.appendChild(newdiv);
        
        //the "g" that contains "foreignobject" 
        var newg = document.createElementNS("http://www.w3.org/2000/svg", "g");
        newg.style.cssText = `margin-top: 15px; border: 20px solid green`;

        newg.appendChild(newforeignObject);
        newsvg.appendChild(newg);
        
        j++;
        
     }   
        
        newTdSvg.appendChild(newsvg);
        
      	newTr.appendChild(newTdSvg);
    	
        variants.appendChild(newTr);
			
    }
    
    body.appendChild(variants);
   	
 
}

    return view;
}());