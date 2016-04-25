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
        var baseUrl = defineBaseURL(ext);
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
            return false;
        });
        $("#demo").click(function () {
            $("#demo").attr('class', 'section-active');
            $("#rest").attr('class', 'section-not-active');
            $("#about").attr('class', 'section-not-active');
            $("#doc").attr('class', 'section-not-active');

            $("#subTitle").html("Demo");
            $("#subTitle").show();

            $("#divDemo").show();
            $("#divDoc").hide();
            $("#divAbout").hide();
            $("#divRestI").hide();
            return false;
        });
    });

    function ShowRequest(formData, jqForm, options) {
        var queryString = $.param(formData);
        $('#requestResult').html('<font color="grey">Requesting server...</font>');
        return true;
    }

    function AjaxError(jqXHR, textStatus, errorThrown) {
        $('#requestResult').html("<font color='red'>Error encountered while requesting the server.<br/>" + jqXHR.responseText + "</font>");
        responseJson = null;
    }

    function htmll(s) {
        return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function submitQuery() {
        var urlLocal = $('#gbdForm').attr('action');
        {
            $.ajax({
                type: 'GET',
                url: urlLocal,
                data: {text: $('#inputTextArea').val()},
                success: SubmitSuccesful,
                error: AjaxError,
                contentType: false
                //dataType: "text"
            });
        }

        $('#requestResult').html('<font color="grey">Requesting server...</font>');
    }

    function SubmitSuccesful(responseText, statusText) {
        var selected = $('#selectedService option:selected').attr('value');

        if (selected == 'processQuantityText') {
            SubmitSuccesfulText(responseText, statusText);
        }
        else if (selected == 'processQuantityXML') {
            //SubmitSuccesfulXML(responseText, statusText);          
        }
        else if (selected == 'processQuantityPDF') {
            //SubmitSuccesfulPDF(responseText, statusText);          
        }
        else if (selected == 'annotateQuantityPDF') {
            //SubmitSuccesfulPDFAnnotated(responseText, statusText);          
        }

    }

    function SubmitSuccesfulText(responseText, statusText) {
        responseJson = responseText;
        console.log(responseJson);
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

                var quantities = [];

                if (measurementType == "value") {
                    var quantity = measurement.quantity;
                    if (quantity)
                        quantities.push(quantity)
                }
                else if (measurementType == "interval") {
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
                }
                else {
                    quantities = measurement.quantities;
                }

                if (quantities) {
                    var quantityMap = new Array();
                    for (var currentQuantityIndex = 0; currentQuantityIndex < quantities.length; currentQuantityIndex++) {
                        var quantity = quantities[currentQuantityIndex];
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
                        var start = parseInt(quantity.offsetStart, 10);
                        var end = parseInt(quantity.offsetEnd, 10);
                        if ((startUnit != -1) && ( (startUnit == end) || (startUnit == end + 1)))
                            end = endUnit;
                        if ((endUnit != -1) && ( (endUnit == start) || (endUnit + 1 == start)))
                            start = startUnit;

                        if (start < pos) {
                            // we have a problem in the initial sort of the quantities
                            // the server response is not compatible with the present client 
                            console.log("Sorting of quantities as present in the server's response not valid for this client.");
                            // note: this should never happen?
                        }
                        else {
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
                }
                else if (measurementType == "interval") {
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
                }
                else {
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
            string = toHtml(quantityMap, measurementType);
        } else if (quantityMap.length == 2) {
            measurementType = "Interval";
            string = intervalToHtml(quantityMap, measurementType);
        } else {
            measurementType = "List";
            string = toHtml(quantityMap, measurementType);
        }


        $('#detailed_annot-0').html(string);
        $('#detailed_annot-0').show();
    }

    /*$(document).ready(function() {
     $(document).on('shown', '#xmlCode', function(event) {
     prettyPrint();
     });
     });*/

    function intervalToHtml(quantityMap, measurementType) {
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

        string += "<div class='info-sense-box " + colorLabel + "'><h2 style='color:#FFF;padding-left:10px;font-size:16;'>" + measurementType;
        string += "</h2>";
        string += "<div class='container-fluid' style='background-color:#F9F9F9;color:#70695C;border:padding:5px;margin-top:5px;'>" +
            "<table style='width:100%;background-color:#fff;border:0px'><tr style='background-color:#fff;border:0px;'><td style='background-color:#fff;border:0px;font-size:14;'>";

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

        string += "</td><td style='align:right;bgcolor:#fff'></td></tr>";
        string += "</table></div>";

        return string;

    }

    function toHtml(quantityMap, measurementType) {
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

            var rawValue = quantity.rawValue;
            var unit = quantity.rawUnit;

            var parsedValue = quantity.parsedValue;
            // var parsedUnit = quantity.parsedUnit;

            var normalizedQuantity = quantity.normalizedQuantity;
            var normalizedUnit = quantity.normalizedUnit;

            var rawUnitName = null;
            var startUnit = -1;
            var endUnit = -1;
            if (unit) {
                rawUnitName = unit.name;
                startUnit = parseInt(unit.offsetStart, 10);
                endUnit = parseInt(unit.offsetEnd, 10);
            }

            if (first) {
                string += "<div class='info-sense-box " + colorLabel + "'><h2 style='color:#FFF;padding-left:10px;font-size:16;'>" + measurementType;
                string += "</h2>";
                first = false;
            }

            string += "<div class='container-fluid' style='background-color:#F9F9F9;color:#70695C;border:padding:5px;margin-top:5px;'>" +
                "<table style='width:100%;background-color:#fff;border:0px'><tr style='background-color:#fff;border:0px;'><td style='background-color:#fff;border:0px;font-size:14;'>";

            if (type) {
                string += "<p>quantity type: <b>" + type + "</b></p>";
            }

            if (rawValue) {
                string += "<p>raw value: <b>" + rawValue + "</b></p>";
            }

            if (parsedValue && (parsedValue != rawValue)) {
                string += "<p>parsed value: <b>" + parsedValue + "</b></p>";
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

            string += "</td><td style='align:right;bgcolor:#fff'></td></tr>";
            string += "</table></div>";
        }
        string += "</div>";

        return string;
    }

    function processChange() {
        var selected = $('#selectedService option:selected').attr('value');

        if (selected == 'processQuantityText') {
            createInputTextArea();
            //$('#consolidateBlock').show();
            setBaseUrl('processQuantityText');
        }
        else if (selected == 'processQuantityXML') {
            createInputFile(selected)
            //$('#consolidateBlock').show();
            setBaseUrl('processQuantityXML');
        }
        else if (selected == 'processQuantityPDF') {
            createInputFile(selected);
            //$('#consolidateBlock').hide();
            setBaseUrl('processQuantityPDF');
        }
        else if (selected == 'annotateQuantityPDF') {
            createInputFile(selected);
            //$('#consolidateBlock').hide();
            setBaseUrl('annotateQuantityPDF');
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

    var examples = ["A 20kg ingot is made in a high frequency induction melting furnace and forged to 30mm in thickness and 90mm in width at 850 to 1,150°C. Specimens No.2 to 4, 6 and 15 are materials embodying the invention. Others are for comparison. No.1 is a material equivalent to ASTM standard A469-88 class 8 for generator rotor shaft material. No. 5 is a material containing relatively high Al content. \n\n\
These specimens underwent heat treatment by simulating the conditions for the large size rotor shaft centre of a large capacity generator. First, it was heated to 840°C to form austenite structure and cooled at the speed of 100°C/hour to harden. Then, the specimen was heated and held at 575 to 590°C for 32 hours and cooled at a speed of 15°C/hour. Tempering was done at such a temperature to secure tensile strength in the range of 100 to 105kg/mm2 for each specimen.",
        "The cells were washed three times with RPMI1640  medium (Nissui Pharmaceutical Co.). The cells (1 x107) were incubated in RPMI-1640 medium containing 10% calf fetal serum (Gibco Co.), 50 µg/ml streptomycin, 50 IU/ml of penicillin, 2-mercaptoethanol (5 x 10-5 M), sheep red blood cells (5 x 106 cells) and a test compound dissolved in dimethyl sulfoxide supplied on a microculture plate (NUNC Co., 24 wells) in a carbon dioxide gas incubator (TABAI ESPEC CORP) at 37°C for 5 days.\n\n\
        A solution of 1.18 g (4.00 mmols) of the Compound a obtained in Reference Example 1, 0.39 g (4.13 mmols) of 4-aminopyridine and 20 ml of toluene was heated to reflux for 2 hours. After cooling, the reaction mixture was poured into 1 N sodium hydroxide aqueous solution, and washed twice with chloroform. 2 N Hydrochloric acid aqueous solution was added to the aqueous layer and the precipitated white crystals were filtered and dried to give 0.73 g (yield: 53%) of Compound 3.",
        "Fifty-three journals were collected: 13 were eliminated from analysis, because they were incomplete, unclear or unreadable. 40 journals were analysed: 19 were journals of subjects of race Z (4 women and 15 men, 30 ± 10 years, 176 ± 7 cm, 70 ± 9 kg, 15 ± 5 % of fat mass, VO 2max : 50 ± 8 ml · kg −1 · min −1 and 21 of race A (6 women and 15 men, 40 ± 7 years, 176 ± 7 cm, 72 ± 10 kg, 18 ± 8 % fat mass, VO 2max : 58 ± 8 ml · kg −1 · min −1 ). Energy, macronutrients (CHO, fat and proteins) and liquid intakes were analysed."]

})(jQuery);



