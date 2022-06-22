space = function() {
	
	var view = {};
	var _representation, _value;
	var knimeTable = null;
    var previewTable = null;
    var previewDataTable = null;
    var nominalTable = null;
    var nominalDataTable = null;
	var dataTable = null;
	var selection = {};
	var allCheckboxes = [];
	var initialized = false;
    var svgWidth = 120;
    var svgHeight = 30;
    var svgWsmall = 120;
    var svgHsmall = 30;
    var svgWbig = 500;
    var svgHbig = 300;
    var xScale, yScale, xScaleNom;
    var xAxis, yAxis, barsScale;
    var margin = {top:0.8*svgHeight, left: 0.5*svgWidth, bottom: 0.7*svgHeight, right:0.2*svgHeight};
    var content;
    var hideUnselected = false;
    var histSizes = [];
    var histNomSizes = [];
    var histCol;
    var histColNom;
    var pageLength;
    var pageLengths;
    var order = [];
    var buttons = [];
    var respOpenedNum = new Map();
    var respOpenedNom = new Map();
    var showWarningMessage = true;
    var warningMessageCutOffValues = "Some nominal values were cut off by the number of unique nominal values. Change settings in the dialog window.";
    var prevRowsPerPage;
    var openedNomRows = 0;
    var openedNumRows = 0;
    
    
	
	
	
	view.init = function(representation, value) {
		if (!representation || !representation.statistics) {
			$('body').append("p").append("Error: No data available");
			return;
		}
		_representation = representation;
	    _value = value;
	    if (parent && parent.KnimePageLoader) {
			_init();
		} else {
			$(document).ready(function() {
                _init();
            });
		}
	}
	
	_init = function() {
		var tabs = $('<div />').attr('id', 'tabs').attr('class', 'knime-tab').appendTo('body');
        var listOfTabNames = $('<ul />').attr("class", "nav nav-tabs").attr('role', 'tabList').appendTo(tabs);
        content = $('<div />').attr('class', 'tab-content').appendTo(tabs);
        
        $('<li class="active"><a href="#tabs-knimeDataExplorerContainer" data-toggle="tab" aria-expanded="true" class="knime-label">' + 'Numeric' + '</a></li>').appendTo(listOfTabNames);
        
        $('<li class=""><a href="#tabs-knimeNominalContainer" data-toggle="tab" aria-expanded="false" class="knime-label">' + 'Nominal' + '</a></li>').appendTo(listOfTabNames);

        $('<li class=""><a href="#tabs-knimePreviewContainer" data-toggle="tab" aria-expanded="false" class="knime-label">' + 'Data Preview' + '</a></li>').appendTo(listOfTabNames);
        
        
        _setControlCssStyles();
        
        $('a[data-toggle="tab"]').on( 'shown.bs.tab', function (e) {
            var table = $.fn.dataTable.tables( {visible: true, api: true} );
            table.columns.adjust().responsive.recalc();
            
            //artificial action of the empty table to represent that no data avaliable in the table correctly
            if (table.rows()[0].length == 0) {
                table.order([0, 'asc']).draw(true);
            }
        });
	}
    
    
    
    mapInitFill = function(map, num) {
        map.set(num, {});
        map.get(num).col = [];
        map.get(num).colNum = 0;
    }
    
    
	
    
    
    
    
    isInt = function(n) {
       return n % 1 === 0;
    }
	
	
	
	applyViewValue = function(jsDataTable) {
		if (_representation.enableSearching && _value.filterString) {
			jsDataTable.search(_value.filterString);
		}
		if (_representation.enablePaging && _value.currentPage) {
			setTimeout(function() {
				jsDataTable.page(_value.currentPage).draw('page');
			}, 0);
		}
	}
	
	finishInit = function(jsDataTable) {
		allCheckboxes = jsDataTable.column(1).nodes().to$().find('input[type="checkbox"]');
		initialized = true;
	}
	
	
	view.validate = function() {
	    return true;
	}
	
	view.getComponentValue = function() {
		if (!_value) {
			return null;
		}
		_value.selection = [];
		for (var id in selection) {
			if (selection[id]) {
				_value.selection.push(id);
			}
		}
		if (_value.selection.length == 0) {
			_value.selection = null;
		}
		var pageNumber = dataTable.page();
		if (pageNumber > 0) {
			_value.currentPage = pageNumber;
		}
		var pageSize = dataTable.page.len();
		if (pageSize != _representation.initialPageSize) {
			_value.pageSize = pageSize;
		}
		var searchString = dataTable.search();
		if (searchString.length) {
			_value.filterString = searchString;
		}
		var order = dataTable.order();
		if (order.length > 0) {
			_value.currentOrder = order;
		}
		if (_representation.enableColumnSearching) {
			_value.columnFilterStrings = [];
			var filtered = false;
			dataTable.columns().every(function (index) {
		        var input = $('input', this.footer());
		        if (input.length) {
		        	var filterString = input.val();
		        	_value.columnFilterStrings.push(filterString);
		        	filtered |= filterString.length;
		        } else {
		        	_value.columnFilterStrings.push("");
		        }
		    });
			if (!filtered) {
				_value.columnFilterStrings = null;
			}
		}
		hideUnselected = document.getElementById('showSelectedOnlyCheckbox');
		if (hideUnselected) {
			_value.hideUnselected = hideUnselected.checked;
		}
		return _value;
	}
	
	return view;
	
}();