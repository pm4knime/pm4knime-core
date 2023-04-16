varExplorer = (function() {

    let _representation;
    let _value;
    let _svg;

    let view = {};

    view.init = function(representation, value) {
        _representation = representation;
        _value = value;

        createUI(representation.data, representation.variants);
        visu();
    };


    view.getComponentValue = () => {
        return _value;
    };

    view.getSVG = () => {
        return _svg;

    };

    function createUI(data, tracevariants) {

        let body = document.getElementsByTagName("body")[0];

        var variants = document.createElement('table');
        variants.setAttribute("id", "varianttable");

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

            let currentIndex = array.length,
                randomIndex;

            // While there remain elements to shuffle.
            while (currentIndex != 0) {

                // Pick a remaining element.
                randomIndex = Math.floor(Math.random() * currentIndex);
                currentIndex--;

                // And swap it with the current element.

                [array[currentIndex], array[randomIndex]] = [
                    array[randomIndex], array[currentIndex]
                ];

            }

            return array;
        }


        var hues = colors.map(x => x.substring(4, x.length - 1)
            .replace(/ /g, '')
            .split(',')[0]).map(x => parseInt(x));

        var brightnesses = hues.map(x => (x == 0 || (x >= 220 && x <= 280)) ? 0 : 1);

        for (var i = 0; i < tracevariants.variants.length; i++) {
            let trace = tracevariants.variants[i].activities;
            let freq = tracevariants.variants[i].frequency;
            let pct = (freq / totalfrequency * 100).toFixed(2);

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

            var svg_dymwidth = (trace.length * 160) + 160;

            newsvg.setAttributeNS(null, "width", svg_dymwidth);

            var j = trace.length - 1;

            while (j >= 0) {
                var loopg = document.createElementNS("http://www.w3.org/2000/svg", "g");
                //the "polygon" that will contain the polygon object with coordinates
                var newpolygon = document.createElementNS("http://www.w3.org/2000/svg", "polygon");


                if (j === 0) {
                    newpolygon.setAttribute("points", "5,10 " + "5,55 " + "145,55 " + "160,32.5 " + "145,10");

                } else {
                    newpolygon.setAttribute("points", (160 * j - 10) + ",10 " +
                        (145 + 160 * j) + ",10 " +
                        (160 + 160 * j) + ",32.5 " +
                        (145 + 160 * j) + ",55 " +
                        (160 * j - 10) + ",55 " +
                        (160 * j + 5) + ", 32.5 ");
                }



                newpolygon.style.fill = colors[tracevariants.activities.indexOf(trace[j])];


                var hoverg = document.createElementNS("http://www.w3.org/2000/svg", "g");
                var newtext = document.createElementNS("http://www.w3.org/2000/svg", "text");
                newtext.textContent = trace[j];
                newtext.style.display = "none";
                newtext.setAttribute("fill", "white");
                newtext.setAttribute("x", (145 + 160 * j) + 10);
                newtext.setAttribute("y", 32.5 - 15);
                newtext.setAttribute("text-anchor", "middle");

                var newrect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
                hoverg.appendChild(newrect);
                hoverg.appendChild(newtext);

                //the "div" that contains the name of activity
                var newdiv = document.createElement('div');
                newdiv.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");

                //to assign the style of newdiv
                newdiv.style.cssText = brightnesses[tracevariants.activities.indexOf(trace[j])] == 1 ? `color: black;width: 130px; height: 30px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap;font-size:15px; margin-top: 1px; margin-left: 1px; padding-top: 5px; text-align: center` : `color: black;width: 130px; height: 30px; overflow:hidden; text-overflow: ellipsis; white-space: nowrap;font-size:15px; margin-top: 1px; margin-left: 1px; padding-top: 5px; text-align: center`;
                newdiv.innerHTML += `${trace[j]}`;
                //newdiv.style.color = brightnesses[tracevariants.activities.indexOf(trace[j])];

                //the "foreignobject" that will contain the above div 
                var newforeignObject = document.createElementNS("http://www.w3.org/2000/svg", 'foreignObject');

                //to set the attributes of newforeignObject
                newforeignObject.setAttribute("x", 10 + 160 * j);
                newforeignObject.setAttribute("y", 15);
                newforeignObject.setAttribute("width", "135px");
                newforeignObject.setAttribute("height", "35px");
                newforeignObject.style.cssText = ` align-items: center; display: flex;`;

                newforeignObject.appendChild(newdiv);

                //the "g" that contains "foreignobject" 
                var newg = document.createElementNS("http://www.w3.org/2000/svg", "g");
                newg.style.cssText = `margin-top: 15px; border: 20px solid green`;

                newg.appendChild(newforeignObject);


                loopg.appendChild(newpolygon);
                loopg.appendChild(newg);
                loopg.appendChild(hoverg);


                //add the mouseover and mouseout event listeners to show/hide the text element
                loopg.addEventListener("mouseover", function() {
                    var text = this.getElementsByTagName("g")[1].getElementsByTagName("text")[0];
                    text.style.display = "block";
                    var rect = this.getElementsByTagName("g")[1].getElementsByTagName("rect")[0];
                    rect.setAttribute("x", text.getBBox().x - 2);
                    rect.setAttribute("y", text.getBBox().y - 2);
                    rect.setAttribute("width", text.getBBox().width + 4);
                    rect.setAttribute("height", text.getBBox().height + 4);
                    rect.setAttribute("fill", "gray");
                });
                loopg.addEventListener("mouseout", function() {
                    this.getElementsByTagName("g")[1].getElementsByTagName("text")[0].style.display = "none";
                    this.getElementsByTagName("g")[1].getElementsByTagName("rect")[0].setAttribute("fill", "none");

                });


                newsvg.appendChild(loopg);

                j--;

            }

            newTdSvg.appendChild(newsvg);

            newTr.appendChild(newTdSvg);

            variants.appendChild(newTr);

        }

        //<button id="export-btn">Export as image</button>
        exporta = document.createElement('a');
        exporta.id = "export-a";
        exportbtn = document.createElement('button');
        exportbtn.id = "export-btn";
        exportbtn.innerHTML += `Export`;
        exporta.appendChild(exportbtn)
        body.appendChild(exporta);

        body.appendChild(variants);

    }


    function visu() {
        var table = document.getElementById("varianttable");
        var exportA = document.getElementById("export-a");

        domtoimage.toSvg(table, {
                bgcolor: 'white'
            })
            .then(function(dataUrl) {

                console.log("print dataUrl");
                console.log(dataUrl);
                exportA.href = dataUrl;
                exportA.download = "varianttable.svg";
                var img = new Image();
                img.src = dataUrl;
                img.onload = function() {
                    var canvas = document.createElement("canvas");
                    var originalWidth = img.width;
                    var originalHeight = img.height;
                    var maxWidth = 10000;
                    var maxHeight = 10000;
                    var widthScaleFactor = maxWidth / originalWidth;
                    var heightScaleFactor = maxHeight / originalHeight;
                    var scaleFactor = Math.min(widthScaleFactor, heightScaleFactor, 10);
                    var scaleFactor = Math.max(scaleFactor, 5);
                    console.log("scaleFactor");
                    console.log(scaleFactor);
                    canvas.width = originalWidth * scaleFactor;
                    canvas.height = originalHeight * scaleFactor;
                    console.log("canvas.width");
                    console.log(canvas.width);
                    console.log("canvas.height");
                    console.log(canvas.height);
                    var ctx = canvas.getContext("2d");
                    ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
                    var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
                    svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
                    svg.setAttribute("width", originalWidth);
                    svg.setAttribute("height", originalHeight);
                    var image = document.createElementNS("http://www.w3.org/2000/svg", "image");
                    var actualLink = exportA.getAttribute('href');

                    image.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", canvas.toDataURL("image/png"));
                    image.setAttribute("width", originalWidth);
                    image.setAttribute("height", originalHeight);
                    svg.appendChild(image);
                    _svg = (new XMLSerializer()).serializeToString(svg);
                    console.log('print stringSVG ', _svg);
                };

            })
            .catch(function(error) {
                console.error('oops, something went wrong!', error);
            });
    }
    return view;
}());