/**
 *  Javascript functions for the front end.
 *
 *  Author: Patrice Lopez
 */

var grobid = (function ($) {

    // for components view
    var responseJson = null;

    // for associating several quantities to a measurement
    var measurementMap = new Array();

    function defineBaseURL(ext) {
        var baseUrl = null;
        if ($(location).attr('href').indexOf("index.html") != -1)
            baseUrl = $(location).attr('href').replace("index.html", ext);
        else
            baseUrl = $(location).attr('href') + ext;
        return baseUrl;
    }

    function setBaseUrl(ext) {
        var baseUrl = defineBaseURL('service' + '/' + ext);
        $('#gbdForm').attr('action', baseUrl);
    }

    $(document).ready(function () {

        $("#subTitle").html("About");
        $("#divAbout").show();
        $("#divRestI").hide();
        $("#divDoc").hide();
        $('#consolidateBlock').show();

        createInputTextArea('text');
        setBaseUrl('processQuantityText');
        $('#example0').bind('click', function (event) {
            event.preventDefault();
            $('#inputTextArea').val(examples[0]);
        });
        setBaseUrl('processQuantityText');
        $('#example1').bind('click', function (event) {
            event.preventDefault();
            $('#inputTextArea').val(examples[1]);
        });
        $('#example2').bind('click', function (event) {
            event.preventDefault();
            $('#inputTextArea').val(examples[2]);
        });
        $('#example3').bind('click', function (event) {
            event.preventDefault();
            $('#inputTextArea').val(examples[3]);
        });
        $("#selectedService").val('processQuantityText');

        $('#selectedService').change(function () {
            processChange();
            return true;
        });

        $('#submitRequest').bind('click', submitQuery);

        $("#about").click(function () {
            $("#about").attr('class', 'section-active');
            $("#rest").attr('class', 'section-not-active');
            $("#doc").attr('class', 'section-not-active');
            $("#demo").attr('class', 'section-not-active');

            $("#subTitle").html("About");
            $("#subTitle").show();

            $("#divAbout").show();
            $("#divRestI").hide();
            $("#divDoc").hide();
            $("#divDemo").hide();
            $('#requestResult').hide();
            return false;
        });
        $("#rest").click(function () {
            $("#rest").attr('class', 'section-active');
            $("#doc").attr('class', 'section-not-active');
            $("#about").attr('class', 'section-not-active');
            $("#demo").attr('class', 'section-not-active');

            $("#subTitle").hide();
            //$("#subTitle").show();
            processChange();

            $("#divRestI").show();
            $("#divAbout").hide();
            $("#divDoc").hide();
            $("#divDemo").hide();
            return false;
        });
        $("#doc").click(function () {
            $("#doc").attr('class', 'section-active');
            $("#rest").attr('class', 'section-not-active');
            $("#about").attr('class', 'section-not-active');
            $("#demo").attr('class', 'section-not-active');

            $("#subTitle").html("Doc");
            $("#subTitle").show();

            $("#divDoc").show();
            $("#divAbout").hide();
            $("#divRestI").hide();
            $("#divDemo").hide();
            $('#requestResult').hide();
            return false;
        });
    });

    function ShowRequest(formData, jqForm, options) {
        var queryString = $.param(formData);
        $('#requestResult').html('<font color="grey">Requesting server...</font>');
        return true;
    }

    function showError(jqXHR, textStatus, errorThrown) {
        $('#requestResult').html("<font color='red'>Error encountered while requesting the server.<br/>" + jqXHR.responseText + "</font>");
        responseJson = null;
    }

    function showError(statusCode, message) {
        message = "Error: " + statusCode + ", " + message + " - The PDF document cannot be annotated. Please check the server logs.";
        $('#infoResult').html("<font color='red'>Error encountered while requesting the server.<br/>" + message + "</font>");
        responseJson = null;
        return true;
    }

    function showError(message) {
        if (!message)
            message = "Error: ";
        message += " - The PDF document cannot be annotated. Please check the server logs.";
        $('#infoResult').html("<font color='red'>Error encountered while requesting the server.<br/>" + message + "</font>");
        responseJson = null;
        return true;
    }

    function htmll(s) {
        return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function submitQuery() {
        var selected = $('#selectedService option:selected').attr('value');
        var urlLocal = $('#gbdForm').attr('action');

        measurementMap = new Array();

        $('#infoResult').html('<font color="grey">Requesting server...</font>');
        $('#requestResult').html('');

        if (selected === 'processQuantityText') {
            var formData = new FormData();
            formData.append("text", $('#inputTextArea').val());

            $.ajax({
                type: 'POST',
                url: urlLocal,
                data: formData,
                success: SubmitSuccesful,
                error: showError,
                contentType: false,
                processData: false
            });
        } else if (selected === 'annotateQuantityPDF') {
            // we will have JSON annotations to be layered on the PDF

            // request for the annotation information
            var form = document.getElementById('gbdForm');
            var formData = new FormData(form);
            var xhr = new XMLHttpRequest();
            var url = $('#gbdForm').attr('action');
            xhr.responseType = 'json';
            xhr.open('POST', url, true);

            var nbPages = -1;
            $('#requestResult').show();

            // display the local PDF
            if ((document.getElementById("input").files[0].type === 'application/pdf') ||
                (document.getElementById("input").files[0].name.endsWith(".pdf")) ||
                (document.getElementById("input").files[0].name.endsWith(".PDF")))
                var reader = new FileReader();
            reader.onloadend = function () {
                // to avoid cross origin issue
                //PDFJS.disableWorker = true;
                var pdfAsArray = new Uint8Array(reader.result);
                // Use PDFJS to render a pdfDocument from pdf array
                PDFJS.getDocument(pdfAsArray).then(function (pdf) {
                    // Get div#container and cache it for later use
                    var container = document.getElementById("requestResult");
                    // enable hyperlinks within PDF files.
                    //var pdfLinkService = new PDFJS.PDFLinkService();
                    //pdfLinkService.setDocument(pdf, null);

                    //$('#requestResult').html('');
                    nbPages = pdf.numPages;

                    // Loop from 1 to total_number_of_pages in PDF document
                    for (var i = 1; i <= nbPages; i++) {

                        // Get desired page
                        pdf.getPage(i).then(function (page) {
                            var table = document.createElement("table");
                            var tr = document.createElement("tr");
                            var td1 = document.createElement("td");
                            var td2 = document.createElement("td");

                            tr.appendChild(td1);
                            tr.appendChild(td2);
                            table.appendChild(tr);

                            var div0 = document.createElement("div");
                            div0.setAttribute("style", "text-align: center; margin-top: 1cm; width:80%;");
                            var pageInfo = document.createElement("p");
                            var t = document.createTextNode("page " + (page.pageIndex + 1) + "/" + (nbPages));
                            pageInfo.appendChild(t);
                            div0.appendChild(pageInfo);

                            td1.appendChild(div0);

                            var scale = 1.5;
                            var viewport = page.getViewport(scale);
                            var div = document.createElement("div");

                            // Set id attribute with page-#{pdf_page_number} format
                            div.setAttribute("id", "page-" + (page.pageIndex + 1));

                            // This will keep positions of child elements as per our needs, and add a light border
                            div.setAttribute("style", "position: relative; ");


                            // Create a new Canvas element
                            var canvas = document.createElement("canvas");
                            canvas.setAttribute("style", "border-style: solid; border-width: 1px; border-color: gray;");

                            // Append Canvas within div#page-#{pdf_page_number}
                            div.appendChild(canvas);

                            // Append div within div#container
                            td1.appendChild(div);

                            var annot = document.createElement("div");
                            annot.setAttribute('style', 'vertical-align:top;');
                            annot.setAttribute('id', 'detailed_annot-' + (page.pageIndex + 1));
                            td2.setAttribute('style', 'vertical-align:top;');
                            td2.appendChild(annot);

                            container.appendChild(table);

                            var context = canvas.getContext('2d');
                            canvas.height = viewport.height;
                            canvas.width = viewport.width;

                            var renderContext = {
                                canvasContext: context,
                                viewport: viewport
                            };

                            // Render PDF page
                            page.render(renderContext).then(function () {
                                // Get text-fragments
                                return page.getTextContent();
                            })
                                .then(function (textContent) {
                                    // Create div which will hold text-fragments
                                    var textLayerDiv = document.createElement("div");

                                    // Set it's class to textLayer which have required CSS styles
                                    textLayerDiv.setAttribute("class", "textLayer");

                                    // Append newly created div in `div#page-#{pdf_page_number}`
                                    div.appendChild(textLayerDiv);

                                    // Create new instance of TextLayerBuilder class
                                    var textLayer = new TextLayerBuilder({
                                        textLayerDiv: textLayerDiv,
                                        pageIndex: page.pageIndex,
                                        viewport: viewport
                                    });

                                    // Set text-fragments
                                    textLayer.setTextContent(textContent);

                                    // Render text-fragments
                                    textLayer.render();
                                });
                        });
                    }
                });
            };
            reader.readAsArrayBuffer(document.getElementById("input").files[0]);

            xhr.onreadystatechange = function (e) {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    var response = e.target.response;
                    //var response = JSON.parse(xhr.responseText);
                    //console.log(response);
                    setupAnnotations(response);
                } else if (xhr.status !== 200) {
                    showError(statusCode=xhr.status, message=xhr.statusText);
                }
            };
            xhr.send(formData);
        }
    }

    function SubmitSuccesful(responseText, statusText) {
        var selected = $('#selectedService option:selected').attr('value');

        if (selected === 'processQuantityText') {
            SubmitSuccesfulText(responseText, statusText);
        } else if (selected === 'processQuantityXML') {
            SubmitSuccessfulXML(responseText, statusText);
        } else if (selected === 'annotateQuantityPDF') {
            SubmitSuccessfulPDF(responseText, statusText);
        }

    }

    function SubmitSuccesfulText(responseText, statusText) {
        responseJson = responseText;
        //console.log(responseJson);
        $('#infoResult').html('');
        if ((responseJson == null) || (responseJson.length == 0)) {
            $('#requestResult')
                .html("<font color='red'>Error encountered while receiving the server's answer: response is empty.</font>");
            return;
        }

        //responseJson = jQuery.parseJSON(responseJson);

        var display = '<div class=\"note-tabs\"> \
            <ul id=\"resultTab\" class=\"nav nav-tabs\"> \
                <li class="active"><a href=\"#navbar-fixed-annotation\" data-toggle=\"tab\">Annotations</a></li> \
                <li><a href=\"#navbar-fixed-json\" data-toggle=\"tab\">Response</a></li> \
            </ul> \
            <div class="tab-content"> \
            <div class="tab-pane active" id="navbar-fixed-annotation">\n';

        display += '<pre style="background-color:#FFF;width:95%;" id="displayAnnotatedText">';

        var string = $('#inputTextArea').val();
        var newString = "";
        var lastMaxIndex = string.length;

        display += '<table id="sentenceNER" style="width:100%;table-layout:fixed;" class="table">';
        //var string = responseJson.text;

        display += '<tr style="background-color:#FFF;">';
        var measurements = responseJson.measurements;
        if (measurements) {
            var pos = 0; // current position in the text

            for (var currentMeasurementIndex = 0; currentMeasurementIndex < measurements.length; currentMeasurementIndex++) {
                var measurement = measurements[currentMeasurementIndex];
                var measurementType = measurement.type;
                var substance = measurement.quantified;

                var quantities = [];

                if (measurementType == "value") {
                    var quantity = measurement.quantity;
                    if (quantity) {
                        quantities.push(quantity)
                    }
                } else if (measurementType == "interval") {
                    var quantityLeast = measurement.quantityLeast;
                    if (quantityLeast) {
                        quantities.push(quantityLeast);
                    }
                    var quantityMost = measurement.quantityMost;
                    if (quantityMost) {
                        quantities.push(quantityMost);
                    }
                    if (!quantityLeast && !quantityMost) {
                        var quantityBase = measurement.quantityBase;
                        if (quantityBase) {
                            quantities.push(quantityBase);
                        }
                        var quantityRange = measurement.quantityRange;
                        if (quantityRange) {
                            quantities.push(quantityRange);
                        }
                    }
                } else {
                    quantities = measurement.quantities;
                }

                if (quantities) {
                    var quantityMap = new Array();
                    for (var currentQuantityIndex = 0; currentQuantityIndex < quantities.length; currentQuantityIndex++) {
                        var quantity = quantities[currentQuantityIndex];
                        quantity['quantified'] = substance;
                        quantityMap[currentQuantityIndex] = quantity;
                        var quantityType = quantity.type;
                        var value = quantity.value;
                        var rawValue = quantity.rawValue;
                        var unit = quantity.rawUnit;
                        var rawUnitName = null;
                        var unitName = null;
                        var startUnit = -1;
                        var endUnit = -1;
                        if (unit) {
                            rawUnitName = unit.rawName;
                            unitName = unit.name;
                            startUnit = parseInt(unit.offsetStart, 10);
                            endUnit = parseInt(unit.offsetEnd, 10);
                        }
                        if (quantityType)
                            quantityType = quantityType.replaceAll(" ", "_");
                        var start = parseInt(quantity.offsetStart, 10);
                        var end = parseInt(quantity.offsetEnd, 10);
                        if ((startUnit != -1) && ((startUnit == end) || (startUnit == end + 1)))
                            end = endUnit;
                        if ((endUnit != -1) && ((endUnit == start) || (endUnit + 1 == start)))
                            start = startUnit;

                        if (start < pos) {
                            // we have a problem in the initial sort of the quantities
                            // the server response is not compatible with the present client 
                            console.log("Sorting of quantities as present in the server's response not valid for this client.");
                            // note: this should never happen?
                        } else {
                            newString += string.substring(pos, start)
                                + ' <span id="annot-' + currentMeasurementIndex + '-' + currentQuantityIndex + '" rel="popover" data-color="' + quantityType + '">'
                                + '<span class="label ' + quantityType + '" style="cursor:hand;cursor:pointer;" >'
                                + string.substring(start, end) + '</span></span>';
                            pos = end;
                        }
                    }
                    measurementMap[currentMeasurementIndex] = quantityMap;
                }
            }
            newString += string.substring(pos, string.length);
        }

        newString = "<p>" + newString.replace(/(\r\n|\n|\r)/gm, "</p><p>") + "</p>";
        //string = string.replace("<p></p>", "");

        display += '<td style="font-size:small;width:60%;border:1px solid #CCC;"><p>' + newString + '</p></td>';
        display += '<td style="font-size:small;width:40%;padding:0 5px; border:0"><span id="detailed_annot-0" /></td>';

        display += '</tr>';


        display += '</table>\n';


        display += '</pre>\n';


        display += '</div> \
                    <div class="tab-pane " id="navbar-fixed-json">\n';


        display += "<pre class='prettyprint' id='jsonCode'>";

        display += "<pre class='prettyprint lang-json' id='xmlCode'>";
        var testStr = vkbeautify.json(responseText);

        display += htmll(testStr);

        display += "</pre>";
        display += '</div></div></div>';

        $('#requestResult').html(display);
        window.prettyPrint && prettyPrint();

        if (measurements) {
            for (var measurementIndex = 0; measurementIndex < measurements.length; measurementIndex++) {
                var measurement = measurements[measurementIndex];
                var measurementType = measurement.type;
                var quantities = [];

                if (measurementType == "value") {
                    var quantity = measurement.quantity;
                    if (quantity)
                        quantities.push(quantity)
                } else if (measurementType == "interval") {
                    var quantityLeast = measurement.quantityLeast;
                    if (quantityLeast)
                        quantities.push(quantityLeast);
                    var quantityMost = measurement.quantityMost;
                    if (quantityMost)
                        quantities.push(quantityMost);

                    if (!quantityLeast && !quantityMost) {
                        var quantityBase = measurement.quantityBase;
                        if (quantityBase)
                            quantities.push(quantityBase);
                        var quantityRange = measurement.quantityRange;
                        if (quantityRange)
                            quantities.push(quantityRange);
                    }
                } else {
                    quantities = measurement.quantities;
                }

                if (quantities) {
                    for (var quantityIndex = 0; quantityIndex < quantities.length; quantityIndex++) {
                        $('#annot-' + measurementIndex + '-' + quantityIndex).bind('hover', viewQuantity);
                        $('#annot-' + measurementIndex + '-' + quantityIndex).bind('click', viewQuantity);
                    }
                }
            }
        }
        /*for (var key in quantityMap) {
         if (entityMap.hasOwnProperty(key)) {
         $('#annot-'+key).bind('hover', viewQuantity);
         $('#annot-'+key).bind('click', viewQuantity);
         }
         }*/

        $('#detailed_annot-0').hide();

        $('#requestResult').show();
    }

    function setupAnnotations(response) {
        // TBD: we must check/wait that the corresponding PDF page is rendered at this point
        if ((response == null) || (response.length == 0)) {
            $('#infoResult')
                .html("<font color='red'>Error encountered while receiving the server's answer: response is empty.</font>");
            return;
        } else {
            $('#infoResult').html('');
        }

        var json = response;
        var pageInfo = json.pages;

        var page_height = 0.0;
        var page_width = 0.0;

        var measurements = json.measurements;
        if (measurements) {
            // hey bro, this must be asynchronous to avoid blocking the brothers
            measurements.forEach(function (measurement, n) {
                var measurementType = measurement.type;
                var quantities = [];
                var substance = measurement.quantified;

                if (measurementType == "value") {
                    var quantity = measurement.quantity;
                    if (quantity)
                        quantities.push(quantity)
                } else if (measurementType == "interval") {
                    var quantityLeast = measurement.quantityLeast;
                    if (quantityLeast)
                        quantities.push(quantityLeast);
                    var quantityMost = measurement.quantityMost;
                    if (quantityMost)
                        quantities.push(quantityMost);

                    if (!quantityLeast && !quantityMost) {
                        var quantityBase = measurement.quantityBase;
                        if (quantityBase)
                            quantities.push(quantityBase);
                        var quantityRange = measurement.quantityRange;
                        if (quantityRange)
                            quantities.push(quantityRange);
                    }
                } else {
                    quantities = measurement.quantities;
                }

                var quantityType = null;
                if (quantities) {
                    var quantityMap = new Array();
                    for (var currentQuantityIndex = 0; currentQuantityIndex < quantities.length; currentQuantityIndex++) {
                        var quantity = quantities[currentQuantityIndex];
                        quantity['quantified'] = substance;
                        quantityMap[currentQuantityIndex] = quantity;
                        if (quantityType == null)
                            quantityType = quantity.type;
                    }
                }

                measurementMap[n] = quantities;

                //var theId = measurement.type;
                var theUrl = null;
                //var theUrl = annotation.url;
                var pos = measurement.boundingBoxes;
                if ((pos != null) && (pos.length > 0)) {
                    pos.forEach(function (thePos, m) {
                        // get page information for the annotation
                        var pageNumber = thePos.p;
                        if (pageInfo[pageNumber - 1]) {
                            page_height = pageInfo[pageNumber - 1].page_height;
                            page_width = pageInfo[pageNumber - 1].page_width;
                        }
                        annotateEntity(quantityType, thePos, theUrl, page_height, page_width, n, m);
                    });
                }
            });
        }
    }

    function annotateEntity(theId, thePos, theUrl, page_height, page_width, measurementIndex, positionIndex) {
        var page = thePos.p;
        var pageDiv = $('#page-' + page);
        var canvas = pageDiv.children('canvas').eq(0);
        //var canvas = pageDiv.find('canvas').eq(0);;

        var canvasHeight = canvas.height();
        var canvasWidth = canvas.width();
        var scale_x = canvasHeight / page_height;
        var scale_y = canvasWidth / page_width;

        var x = thePos.x * scale_x - 1;
        var y = thePos.y * scale_y - 1;
        var width = thePos.w * scale_x + 1;
        var height = thePos.h * scale_y + 1;

        //make clickable the area
        theId = "" + theId;
        if (theId)
            theId = theId.replace(" ", "_");
        var element = document.createElement("a");
        var attributes = "display:block; width:" + width + "px; height:" + height + "px; position:absolute; top:" +
            y + "px; left:" + x + "px;";
        element.setAttribute("style", attributes + "border:2px solid; border-color: " + getColor(theId) + ";");
        //element.setAttribute("style", attributes + "border:2px solid;");
        element.setAttribute("class", theId);
        element.setAttribute("id", 'annot-' + measurementIndex + '-' + positionIndex);
        element.setAttribute("page", page);
        /*element.setAttribute("data-toggle", "popover");
        element.setAttribute("data-placement", "top");
        element.setAttribute("data-content", "content");
        element.setAttribute("data-trigger", "hover");
        $(element).popover({
            content: "<p>Mesurement Object</p><p>" +theId+"<p>",
            html: true,
            container: 'body'
        });*/

        pageDiv.append(element);

        $('#annot-' + measurementIndex + '-' + positionIndex).bind('hover', viewQuantityPDF);
        $('#annot-' + measurementIndex + '-' + positionIndex).bind('click', viewQuantityPDF);
    }

    function viewQuantity() {
        var localID = $(this).attr('id');

        if (responseJson.measurements == null) {
            return;
        }

        var ind1 = localID.indexOf('-');
        var ind2 = localID.indexOf('-', ind1 + 1);
        var localMeasurementNumber = parseInt(localID.substring(ind1 + 1, ind2));
        var localQuantityNumber = parseInt(localID.substring(ind2 + 1, localID.length));
        if ((measurementMap[localMeasurementNumber] == null) || (measurementMap[localMeasurementNumber].length == 0)) {
            // this should never be the case
            console.log("Error for visualising annotation measurement with id " + localMeasurementNumber
                + ", empty list of measurement");
        } else if ((measurementMap[localMeasurementNumber][localQuantityNumber] == null)) {
            // this should never be the case
            console.log("Error for visualising annotation quantity with id " + localQuantityNumber + " with measurement id " + localMeasurementNumber
                + ", empty list of quantity");
        }

        var quantityMap = measurementMap[localMeasurementNumber];
        var measurementType = null;
        var string = "";
        if (quantityMap.length == 1) {
            measurementType = "Atomic value";
            string = toHtml(quantityMap, measurementType, -1);
        } else if (quantityMap.length == 2) {
            measurementType = "Interval";
            string = intervalToHtml(quantityMap, measurementType, -1);
        } else {
            measurementType = "List";
            string = toHtml(quantityMap, measurementType, -1);
        }

        $('#detailed_annot-0').html(string);
        $('#detailed_annot-0').show();
    }

    function viewQuantityPDF() {
        var pageIndex = $(this).attr('page');
        var localID = $(this).attr('id');

        console.log('viewQuanityPDF ' + pageIndex + ' / ' + localID);

        var ind1 = localID.indexOf('-');
        var ind2 = localID.indexOf('-', ind1 + 1);
        var localMeasurementNumber = parseInt(localID.substring(ind1 + 1, ind2));
        //var localMeasurementNumber = parseInt(localID.substring(ind1 + 1, localID.length));
        if ((measurementMap[localMeasurementNumber] == null) || (measurementMap[localMeasurementNumber].length == 0)) {
            // this should never be the case
            console.log("Error for visualising annotation measurement with id " + localMeasurementNumber
                + ", empty list of measurement");
        }

        var quantityMap = measurementMap[localMeasurementNumber];
        console.log(quantityMap);
        var measurementType = null;
        var string = "";
        if (quantityMap.length == 1) {
            measurementType = "Atomic value";
            string = toHtml(quantityMap, measurementType, $(this).position().top);
        } else if (quantityMap.length == 2) {
            measurementType = "Interval";
            string = intervalToHtml(quantityMap, measurementType, $(this).position().top);
        } else {
            measurementType = "List";
            string = toHtml(quantityMap, measurementType, $(this).position().top);
        }
//console.log(string); 
        $('#detailed_annot-' + pageIndex).html(string);
        $('#detailed_annot-' + pageIndex).show();
    }

    function intervalToHtml(quantityMap, measurementType, topPos) {
        var string = "";
        var rawUnitName = null;

        // LEAST value
        var quantityLeast = quantityMap[0];
        var type = quantityLeast.type;

        var colorLabel = null;
        if (type) {
            colorLabel = type;
        } else {
            colorLabel = quantityLeast.rawName;
        }
        if (colorLabel)
            colorLabel = colorLabel.replaceAll(" ", "_");
        var leastValue = quantityLeast.rawValue;
        var startUniLeast = -1;
        var endUnitLeast = -1;

        var unitLeast = quantityLeast.rawUnit;
        if (unitLeast) {
            rawUnitName = unitLeast.name;
            startUniLeast = parseInt(quantityLeast.offsetStart, 10);
            endUnitLeast = parseInt(quantityLeast.offsetEnd, 10);
        }
        var normalizedQuantityLeast = quantityLeast.normalizedQuantity;
        var normalizedUnit = quantityLeast.normalizedUnit;

        var substance = quantityLeast.quantified;

        // MOST value
        var quantityMost = quantityMap[1];
        var mostValue = quantityMost.rawValue;
        var startUniMost = -1;
        var endUnitMost = -1;

        var unitMost = quantityMost.rawUnit;
        if (unitMost) {
            startUniMost = parseInt(quantityMost.offsetStart, 10);
            endUnitMost = parseInt(quantityMost.offsetEnd, 10);
        }
        var normalizedQuantityMost = quantityMost.normalizedQuantity;

        if (!substance)
            substance = quantityMost.quantified;

        string += "<div class='info-sense-box " + colorLabel + "'";
        if (topPos != -1)
            string += " style='vertical-align:top; position:relative; top:" + topPos + "'";
        string += "><h2 style='color:#FFF;padding-left:10px;font-size:16;'>" + measurementType;
        string += "</h2>";
        string += "<div class='container-fluid' style='background-color:#FFF;color:#70695C;border:padding:5px;margin-top:5px;'>" +
            "<table style='width:100%;display:inline-table;'><tr style='display:inline-table;'><td>";

        if (type) {
            string += "<p>quantity type: <b>" + type + "</b></p>";
        }

        if (leastValue || mostValue) {
            string += "<p>raw: from <b>" + leastValue + "</b> to <b>" + mostValue + "</b></p>";
        }

        if (rawUnitName) {
            string += "<p>raw unit name: <b>" + rawUnitName + "</b></p>";
        }

        if (normalizedQuantityLeast || normalizedQuantityMost) {
            string += "<p>normalized: from <b>" + normalizedQuantityLeast + "</b> to <b>"
                + normalizedQuantityMost + "</b></p>";
        }

        if (normalizedUnit) {
            string += "<p>normalized unit: <b>" + normalizedUnit.name + "</b></p>";
        }

        if (substance) {
            string += "</td></tr><tr style='width:100%;display:inline-table;'><td style='border-top-width:1px;width:100%;border-top:1px solid #ddd;display:inline-table;'>";
            string += "<p style='display:inline-table;'>quantified (experimental):"
            string += "<table style='width:100%;display:inline-table;'><tr><td>";
            string += "<p>raw: <b>" + substance.rawName;
            string += "</b></p>";
            string += "<p>normalized: <b>" + substance.normalizedName;
            string += "</b></p></td></tr></table>";
            string += "</p>";
        }

        string += "</td><td style='align:right;bgcolor:#fff'></td></tr>";
        string += "</table></div>";

        return string;

    }

    function toHtml(quantityMap, measurementType, topPos) {
        var string = "";
        var first = true;
        for (var quantityListIndex = 0; quantityListIndex < quantityMap.length; quantityListIndex++) {

            var quantity = quantityMap[quantityListIndex];
            var type = quantity.type;

            var colorLabel = null;
            if (type) {
                colorLabel = type;
            } else {
                colorLabel = quantity.rawName;
            }
            
            if (colorLabel)
                colorLabel = colorLabel.replaceAll(" ", "_");
                
            var rawValue = quantity.rawValue;
            var unit = quantity.rawUnit;

            var parsedValue = quantity.parsedValue;
            var parsedValueStructure = quantity.parsedValue.structure;
            // var parsedUnit = quantity.parsedUnit;

            var normalizedQuantity = quantity.normalizedQuantity;
            var normalizedUnit = quantity.normalizedUnit;

            var substance = quantity.quantified;

            var rawUnitName = null;
            var startUnit = -1;
            var endUnit = -1;
            if (unit) {
                rawUnitName = unit.name;
                startUnit = parseInt(unit.offsetStart, 10);
                endUnit = parseInt(unit.offsetEnd, 10);
            }

            if (first) {
                string += "<div class='info-sense-box " + colorLabel + "'";
                if (topPos != -1)
                    string += " style='vertical-align:top; position:relative; top:" + topPos + "'";
                string += "><h2 style='color:#FFF;padding-left:10px;font-size:16;'>" + measurementType;
                string += "</h2>";
                first = false;
            }

            string += "<div class='container-fluid' style='background-color:#FFF;color:#70695C;border:padding:5px;margin-top:5px;'>" +
                "<table style='width:100%;display:inline-table;'><tr style='display:inline-table;'><td>";

            if (type) {
                string += "<p>quantity type: <b>" + type + "</b></p>";
            }

            if (rawValue) {
                string += "<p>raw value: <b>" + rawValue + "</b></p>";
            }

            if (parsedValue) {
                if (parsedValue.numeric && parsedValue.numeric !== rawValue) {
                    string += "<p>parsed value: <b>" + parsedValue.numeric + "</b></p>";
                } else if (parsedValue.parsed && parsedValue.parsed !== rawValue) {
                    string += "<p>parsed value: <b>" + parsedValue.parsed + "</b></p>";
                }
            }

            if (parsedValueStructure) {
                string += "<p>&nbsp;&nbsp; - type: <b>" + parsedValueStructure.type + "</b></p>";
                string += "<p>&nbsp;&nbsp; - formatted: <b>" + parsedValueStructure.formatted + "</b></p>";
            }


            if (rawUnitName) {
                string += "<p>raw unit name: <b>" + rawUnitName + "</b></p>";
            }

            if (normalizedQuantity) {
                string += "<p>normalized value: <b>" + normalizedQuantity + "</b></p>";
            }

            if (normalizedUnit) {
                string += "<p>normalized unit name: <b>" + normalizedUnit.name + "</b></p>";
            }

            if (substance) {
                string += "</td></tr><tr style='width:100%;display:inline-table;'><td style='border-top-width:1px;width:100%;border-top:1px solid #ddd;display:inline-table;'>";
                string += "<p style='display:inline-table;'>quantified (experimental):"
                string += "<table style='width:100%;display:inline-table;'><tr><td>";
                string += "<p>raw: <b>" + substance.rawName;
                string += "</b></p>";
                string += "<p>normalized: <b>" + substance.normalizedName;
                string += "</b></p></td></tr></table>";
                string += "</p>";
            }

            string += "</td></tr>";
            string += "</table></div>";
        }
        string += "</div>";

        return string;
    }

    function SubmitSuccessfulPDF(responseText, statusText) {
        $('#requestResult').html("<p>Not implemented yet ;)</p>");
    }

    function SubmitSuccessfulXML(responseText, statusText) {
        $('#requestResult').html("<p>Not implemented yet ;)</p>");
    }

    function processChange() {
        var selected = $('#selectedService option:selected').attr('value');

        if (selected == 'processQuantityText') {
            createInputTextArea();
            setBaseUrl('processQuantityText');
            $('#requestResult').hide();
        } else if (selected == 'processQuantityXML') {
            createInputFile(selected)
            setBaseUrl('processQuantityXML');
            $('#requestResult').hide();
        } else if (selected == 'annotateQuantityPDF') {
            createInputFile(selected);
            setBaseUrl('annotateQuantityPDF');
            $('#requestResult').hide();
        }
    }

    function createInputFile(selected) {
        //$('#label').html('&nbsp;'); 
        $('#textInputDiv').hide();
        //$('#fileInputDiv').fileupload({uploadtype:'file'});
        //$('#fileInputDiv').fileupload('reset');
        $('#fileInputDiv').show();

        $('#gbdForm').attr('enctype', 'multipart/form-data');
        $('#gbdForm').attr('method', 'post');
    }

    function createInputTextArea() {
        //$('#label').html('&nbsp;'); 
        $('#fileInputDiv').hide();
        //$('#input').remove();

        //$('#field').html('<table><tr><td><textarea class="span7" rows="5" id="input" name="'+nameInput+'" /></td>'+
        //"<td><span style='padding-left:20px;'>&nbsp;</span></td></tr></table>");
        $('#textInputDiv').show();

        //$('#gbdForm').attr('enctype', '');
        //$('#gbdForm').attr('method', 'post');
    }

    var mapColor = {
        'area': '#87A1A8',
        'volume': '#c43c35',
        'velocity': '#c43c35',
        'fraction': '#c43c35',
        'length': '#01A9DB',
        'time': '#f89406',
        'mass': '#c43c35',
        'temperature': '#398739',
        'frequency': '#8904B1;',
        'concentration': '#31B404'
    };

    /* return a color based on the quantity type */
    function getColor(type) {
        return mapColor[type];
    }

    var examples = ["A 20kg ingot is made in a high frequency induction melting furnace and forged to 30mm in thickness and 90mm in width at 850 to 1,150°C. Specimens No.2 to 4, 6 and 15 are materials embodying the invention. Others are for comparison. No.1 is a material equivalent to ASTM standard A469-88 class 8 for generator rotor shaft material. No. 5 is a material containing relatively high Al content. \n\n\
These specimens underwent heat treatment by simulating the conditions for the large size rotor shaft centre of a large capacity generator. First, it was heated to 840°C to form austenite structure and cooled at the speed of 100°C/hour to harden. Then, the specimen was heated and held at 575 to 590°C for 32 hours and cooled at a speed of 15°C/hour. Tempering was done at such a temperature to secure tensile strength in the range of 100 to 106kg/mm2 for each specimen.",
        "The cells were washed three times with RPMI1640  medium (Nissui Pharmaceutical Co.). The cells (1 x107) were incubated in RPMI-1640 medium containing 10% calf fetal serum (Gibco Co.), 50 µg/ml streptomycin, 50 IU/ml of penicillin, 2-mercaptoethanol (5 x 10-5 M), sheep red blood cells (5 x 106 cells) and a test compound dissolved in dimethyl sulfoxide supplied on a microculture plate (NUNC Co., 24 wells) in a carbon dioxide gas incubator (TABAI ESPEC CORP) at 37°C for 5 days.\n\n\
        A solution of 1.18 g (4.00 mmols) of the Compound a obtained in Reference Example 1, 0.39 g (4.13 mmols) of 4-aminopyridine and 20 ml of toluene was heated to reflux for 2 hours. After cooling, the reaction mixture was poured into 1 N sodium hydroxide aqueous solution, and washed twice with chloroform. 2 N Hydrochloric acid aqueous solution was added to the aqueous layer and the precipitated white crystals were filtered and dried to give 0.73 g (yield: 53%) of Compound 3.",
        "Fifty-three journals were collected: 13 were eliminated from analysis, because they were incomplete, unclear or unreadable. 40 journals were analysed: 19 were journals of subjects of race Z (4 women and 15 men, 30 ± 10 years, 176 ± 7 cm, 70 ± 9 kg, 15 ± 5 % of fat mass, VO 2max : 50 ± 8 ml · kg −1 · min −1 and 21 of race A (6 women and 15 men, 40 ± 7 years, 176 ± 7 cm, 72 ± 10 kg, 18 ± 8 % fat mass, VO 2max : 58 ± 8 ml · kg −1 · min −1 ). Energy, macronutrients (CHO, fat and proteins) and liquid intakes were analysed.",
        "COS-7 cells transfected with the indicated plasmids were lysed in Laemmli sample buffer or the lysis buffer mentioned above. E18.5 mouse brains (ICR) were homogenized in 20 mm HEPES (pH 7.4), 0.1 mm EDTA, 0.1 mm EGTA, 150 mm NaCl, 2 mm MgCl2, 1 mm Na3VO4, 0.4 mm 4-(2-aminoethyl)benzenesulfonyl fluoride hydrochloride, 10 μg/ml leupeptin, and 1 mm dithiothreitol with a Teflon pestle homogenizer. The lysates or homogenates were centrifuged at 15,000 × g for 20 min, and the supernatants were used for immunoprecipitation of Cdk5 with anti-Cdk5 (C8) or anti-p35 (C19). In some cases, immunoprecipitation was performed with anti-Cdk5 (C8) or anti-p35 (C19) that had been cross-linked to protein A-Sepharose beads using the Pierce Crosslink IP kit according to the protocol of the manufacturer. The cell extracts were incubated with 1.5 μg of antibody and 20 μl of protein A-Sepharose beads and rotated overnight at 4 °C. The beads were washed with washing buffer (25 mm Tris-HCl (pH 7.5), 0.1 mm EDTA, 0.1 mm EGTA, 500 mm NaCl, 0.5% Nonidet P-40, and 1 mm dithiothreitol) five times. The kinase activity of Cdk5 was measured with histone H1 as a substrate in kinase buffer (10 mm MOPS (pH 6.8), 1 mm MgCl2, 0.1 mm EDTA, and 0.1 mm EGTA) at 37 °C for 30 min. After SDS-PAGE, phosphorylation was visualized by autoradiography with an imaging plate."]


})(jQuery);



