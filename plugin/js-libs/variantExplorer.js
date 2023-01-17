(varExplorer = function() {

    let _representation;
    let _value;

    let view = {};

    view.init = function(representation, value) {
        _representation = representation;
        _value = value;

        createUI("tttt");
    };

    view.getComponentValue = () => {
        _value.firstName = document.getElementById("firstName").value;
        _value.lastName = document.getElementById("lastName").value;

        return _value;
    };

    function createUI(val1) {
    let body = document.getElementsByTagName("body")[0];
    body.innerHTML = `<div class="container-fluid">
      <form class="form-horizontal">
        <div class="form-group">
          <label class="col-sm-2 control-label">First name</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" id="firstName">
          </div>
        </div>
        
        <div class="form-group">
          <label class="col-sm-2 control-label">Last name</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" id="lastName">
          </div>
        </div>
      </form>
      
        <div class="jumbotron text-center">
             <h1>Table Statistics</h1>
             <p>Number of Rows: ${val1}</p>
        </div>
    </div>`;
}

    return view;
}());