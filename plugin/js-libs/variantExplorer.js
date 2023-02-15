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
    
    
    for (var i = 0; i <tracevariants.variants.length; i++) {
        let trace = tracevariants.variants[i].activities;
        let freq = tracevariants.variants[i].frequency;
        variants.innerHTML += 
        
        	`<tr>
        	
        		<td style ="text-align: right;border: 1px solid black; white-space:nowrap; width:auto; margin-left: auto;">Case</td>
        		
        		<td style ="text-align: left; border: 1px solid black; white-space:nowrap;">
    
    				<svg height="50px" width="120px" border = "1px solid #000000" overflow="hidden" text-overflow = "ellipsis" white-space = "nowrap">
        	
  						<polygon points="5,5 5,45 75,45 100,25 75,5" 	style="fill:rgb(255,255,255);stroke:black;stroke-width:1;  border = 1px solid #000000;"/>
 				
                			<g>
    
    							<foreignObject x="15%" y="30%" width="300" height="50">
	
    								<div style =  "width: 70px; height: 20px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap">I love     SVG!Sorry, your browser does not support inline SVG.
  									</div>
        
    							</foreignObject>
        
    						</g>
                
					</svg>
    
    			</td>
        	
        	</tr>`;
			
    }
    
    body.appendChild(variants);
 
}

    return view;
}());