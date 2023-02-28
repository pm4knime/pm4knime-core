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
	/*
	IDEA TO GENERATE A SEQUENCE OF POLYGONS:
	
	var svg = document.getElementById("svg");
	var polygon = document.createElementNS("http://www.w3.org/2000/svg", "polygon");
	
	svg.appendChild(polygon);

	var array = arr = [ [ 0,0 ], 
    			        [ 50,50 ],
             			[ 25,25 ], ];

	for (value of array) {
  		
  		var point = svg.createSVGPoint();
  		point.x = value[0];
  		point.y = value[1];
  		polygon.points.appendItem(point);
	
	}
	
	*/
    
    for (var i = 0; i <tracevariants.variants.length; i++) {
        let trace = tracevariants.variants[i].activities;
        let freq = tracevariants.variants[i].frequency;
        let pct = (freq/totalfrequency*100).toFixed(2);
        variants.innerHTML += 
        //remove margin-left
			`<tr>
        	
        		<td style ="text-align: right;border: 1px solid black;padding-left:0; white-space:pre;color: #595959;font-size:14px;">${freq} Cases
${pct}% Log</td>
        		
        		<td style ="text-align: left; border: 1px solid black; white-space:nowrap; valign:middle;  align:center;">
    		
    				<svg height="60px" width="120px" border = "1px solid #000000" overflow="hidden" text-overflow = "ellipsis" white-space = "nowrap";>
        	
  						<polygon points="5,10 5,55 75,55 100,32.5 75,10" 	style="fill:rgb(255,255,255);stroke:black;stroke-width:1;  border = 1px solid #000000;"/>
 				
                			<g margin-top="15px">
    		
    							<foreignObject x="15%" y="30%" width="300" height="50">
			
    								<div style =  "width: 70px; height: 30px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap;font-size:20px;">
    								${trace[0]}
  									</div>
        	
    							</foreignObject>
        	
    						</g>
                
					</svg>
    		
    			</td>
        	
        	</tr>`;
			
    }
    
    body.appendChild(variants);
   
    //var dummy = document.createElement('div');
    
    
    //this dummy shows e.g. kind of experimental object
    //dummy.style.cssText = 'border-spacing: 5px 10px; border: 1px solid black; align: left;';
    
    //dummy.innerHTML  += `${totalfrequency}`
    
    //body.appendChild(dummy);
 
}

    return view;
}());