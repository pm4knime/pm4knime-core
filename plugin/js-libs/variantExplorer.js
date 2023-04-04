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
    
    var variants = document.createElement('table');
    
   variants.style.cssText = 'border-spacing: 5px 10px; border: 1px solid black; align: left;';
   variants.style.backgroundColor = '#f2f2f2';
   variants.style.borderColor = 'darkgrey';
    
    var totalfrequency = tracevariants.variants.map(function(value) {
  
  		return value.frequency;
		
	}).reduce((a, b) => a + b, 0);

	//var colors = Array(tracevariants.activities.length).fill().map((_, i) => ("hsl("+ ( ((360 / tracevariants.activities.length) + (i*(tracevariants.activities.length**2))) % 360) + ",85%,40%)")
	
	var activitylen = tracevariants.activities.length;
	
	const colorslist = [
  "#FFB6C1", // lightpink
  "#FFA07A", // lightsalmon
  "#FFDAB9", // peachpuff
  "#FFE4E1", // mistyrose
  "#B0E0E6", // powderblue
  "#ADD8E6", // lightblue
  "#87CEFA", // lightskyblue
  "#00BFFF", // deepskyblue
  "#98FB98", // palegreen
  "#90EE90", // lightgreen
  "#00FA9A", // mediumspringgreen
  "#FA8072", // salmon
  "#FFC0CB", // pink
  "#FFB347", // darkorange
  "#FFD700", // gold
];


    var colors = Array(activitylen).fill().map((_, i) => colorslist[i % colorslist.length]);

	
	function shuffle(array) {
  
  		let currentIndex = array.length,  randomIndex;

  		// While there remain elements to shuffle.
  		while (currentIndex != 0) {

    		// Pick a remaining element.
    		randomIndex = Math.floor(Math.random() * currentIndex);
    		currentIndex--;

    		// And swap it with the current element.
    	
    		[array[currentIndex], array[randomIndex]] = [
      			array[randomIndex], array[currentIndex]];
  			
  			}

  		return array;
		}

    
   	var hues = colors.map(x => x.substring(4, x.length-1)
         .replace(/ /g, '')
         .split(',')[0]).map(x => parseInt(x));
         
    var brightnesses = hues.map(x => (x == 0 || (x >= 220 && x <= 280)) ? 0 : 1);
   
    for (var i = 0; i <tracevariants.variants.length; i++) {
        let trace = tracevariants.variants[i].activities;
        let freq = tracevariants.variants[i].frequency;
        let pct = (freq/totalfrequency*100).toFixed(2);
        
        let tracelen = trace.length;
 
        var newTr = document.createElement('tr'); 
        newTr.style.cssText = `padding: 0; margin: 0`;
        
        var newTdStat = document.createElement('td'); 
        newTdStat.style.cssText = `text-align: right;white-space:pre;color: #595959;font-size:14px;`;
        newTdStat.innerHTML += ` ${freq} Cases <br> ${pct}% Log `;
        newTr.appendChild(newTdStat);
        
        var newTdSvg = document.createElement('td'); 
        newTdSvg.style.cssText = `text-align: left; white-space:nowrap; valign:middle;  align:center; padding: 0; margin: 0;`;
        
        //the "svg" that will contain "polygon"
        var newsvg = document.createElementNS("http://www.w3.org/2000/svg", "svg");  
        
        //to assign the style to the svg
        newsvg.style.cssText = `height: 60px;overflow: hidden; text-overflow: ellipsis; white-space : nowrap;`;
        
        var svg_dymwidth = (trace.length * 160) + 5;
        
        newsvg.setAttributeNS(null,"width", svg_dymwidth);
        
        var firstpolygon = document.createElementNS("http://www.w3.org/2000/svg","polygon");
        
        firstpolygon.setAttribute("points", "5,10 " + "5,55 " + "145,55 " + "160,32.5 " + "145,10");
        
        firstpolygon.style.fill = colors[tracevariants.activities.indexOf(trace[0])];
        
        //firstpolygon.style.fill = "white";
        
        newsvg.appendChild(firstpolygon);
        
        var firstdiv = document.createElement('div');
        
        firstdiv.setAttribute("xmlns","http://www.w3.org/1999/xhtml");
        
        firstdiv.style.cssText = `width: 130px; height: 30px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap;font-size:15px; margin-top: 1px; margin-left: 1px; padding-top: 5px; text-align: center`;
        
        firstdiv.innerHTML += `${trace[0]}`;
        
        firstdiv.style.cssText = brightnesses[tracevariants.activities.indexOf(trace[0])] == 1 ? `color: black;width: 130px; height: 30px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap;font-size:15px; margin-top: 1px; margin-left: 1px; padding-top: 5px; text-align: center` : `color: white;width: 130px; height: 30px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap;font-size:15px; margin-top: 1px; margin-left: 1px; padding-top: 5px; text-align: center`; 

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
        
        newpolygon.setAttribute("points", (160*j - 10)   + ",10 "   + 
        								  (145 + 160*j )  + ",10 "   +
        								  (160 + 160*j ) + ",32.5 " +
        								  (145 + 160*j )  + ",55 "   + 
        								  (160*j - 10)   + ",55 "   + 
        								  (160*j + 5)  + ", 32.5 " );
        								  
       	newpolygon.style.fill = colors[tracevariants.activities.indexOf(trace[j])];

        newsvg.appendChild(newpolygon);

        //the "div" that contains the name of activity
        var newdiv = document.createElement('div');
        newdiv.setAttribute("xmlns","http://www.w3.org/1999/xhtml");
        
        //to assign the style of newdiv
        newdiv.style.cssText = brightnesses[tracevariants.activities.indexOf(trace[j])] == 1 ? `color: black;width: 130px; height: 30px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap;font-size:15px; margin-top: 1px; margin-left: 1px; padding-top: 5px; text-align: center` : `color: white;width: 130px; height: 30px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap;font-size:15px; margin-top: 1px; margin-left: 1px; padding-top: 5px; text-align: center`; 
        newdiv.innerHTML += `${trace[j]}`;
		//newdiv.style.color = brightnesses[tracevariants.activities.indexOf(trace[j])];
        
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