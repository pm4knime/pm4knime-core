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
    let body = document.getElementsByTagName("body")[0];
    body.innerHTML = `<div class="container-fluid">
      <h1>Table Statistics</h1>
    </div>`;
    
    var tablestats = document.createElement('div');
    tablestats.innerHTML = 
            `<p>Number Rows: ${data[0]}</p>
             <p>Number Attributes: ${data[1]}</p>`;
             
    for (var i = 2; i <data.length; i++) {
        let val = data[i];
        tablestats.innerHTML += `<p>Attribute ${i-1}: ${val}</p>`;             
    }
    
    body.appendChild(tablestats);
    
    var variants = document.createElement('div');
    variants.innerHTML = 
            `<p>Trace Variants: </p>`;
             
    for (var i = 0; i <tracevariants.variants.length; i++) {
        let trace = tracevariants.variants[i].activities;
        let freq = tracevariants.variants[i].frequency;
        variants.innerHTML += `<p>${i+1}. Variant with frequency ${freq}: ${trace}</p>`;             
    }
    
    body.appendChild(variants);
}

    return view;
}());