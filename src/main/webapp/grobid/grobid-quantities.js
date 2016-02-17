/**
*  Javascript functions for the front end.
*        
*  Author: Patrice Lopez
*/

//jQuery.fn.prettify = function () { this.html(prettyPrintOne(this.html(),'xml')); };

var grobid = (function($) {

    // for components view
    var responseJson = null;

    // for associating several entities to an annotation position (to support nbest mode visualisation)
    var measurementMap = new Object();

    function defineBaseURL(ext) {
        var baseUrl = null;
        if ( $(location).attr('href').indexOf("index.html") != -1)
            baseUrl = $(location).attr('href').replace("index.html", ext);
        else 
            baseUrl = $(location).attr('href') + ext;
        return baseUrl;
    }

    function setBaseUrl(ext) {
        var baseUrl = defineBaseURL(ext);
        $('#gbdForm').attr('action', baseUrl);
    }
    
    $(document).ready(function() {   
        
        $("#subTitle").html("About");
        $("#divAbout").show();
        $("#divRestI").hide();   
        $("#divDoc").hide();
        $('#consolidateBlock').show();
        
        createInputTextArea('text');
        setBaseUrl('processQuantityText');             
        $("#selectedService").val('processQuantityText');

        $('#selectedService').change(function() {
            processChange();
            return true;
        }); 

        $('#submitRequest').bind('click', submitQuery);

        /*$('#gbdForm').ajaxForm({
            beforeSubmit: ShowRequest,
            data: $('#input').val(),
            success: SubmitSuccesful,
            error: AjaxError,
            dataType: "text"
        });*/
        
        $("#about").click(function() {
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
        $("#rest").click(function() {
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
        $("#doc").click(function() {
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
        $("#demo").click(function() {
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
        $('#requestResult').html("<font color='red'>Error encountered while requesting the server.<br/>"+jqXHR.responseText+"</font>");      
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
              data: { text : $('#textInputArea').val() } ,
              success: SubmitSuccesful,
              error: AjaxError,
              contentType:false  
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
        
        if ( (responseJson == null) || (responseJson.length == 0) ) {
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
        
        var string = $('#textInputArea').val();
        var lastMaxIndex = string.length;
        {    
            display += '<table id="sentenceNER" style="width:100%;table-layout:fixed;" class="table">'; 
            //var string = responseJson.text;
 
                display += '<tr style="background-color:#FFF;">';     
                if (responseJson.measurements) {
                    var currentAnnotationIndex = responseJson.measurements.length-1;
                    for(var m=responseJson.measurements.length-1; m>=0; m--) {
                        /*var entity = responseJson.measurements[m];
                        var domains = entity.domains;
                        var label = null;
                        if (entity.type)
                            label = entity.type;
                        else if (domains && domains.length>0) {
                            label = domains[0].toLowerCase();
                        }
                        else 
                            label = entity.rawName;

                        var start = parseInt(entity.offsetStart,10);
                        var end = parseInt(entity.offsetEnd,10);       
                        
                        if (start > lastMaxIndex) {
                            // we have a problem in the initial sort of the entities
                            // the server response is not compatible with the client 
                            console.log("Sorting of entities as present in the server's response not valid for this client.");
                        }
                        else if (start == lastMaxIndex) {
                            // the entity is associated to the previous map
                            entityMap[currentAnnotationIndex].push(responseJson.entities[m]);
                        }
                        else if (end > lastMaxIndex) {
                            end = lastMaxIndex;
                            lastMaxIndex = start;
                            // the entity is associated to the previous map
                            entityMap[currentAnnotationIndex].push(responseJson.entities[m]);
                        }
                        else {
                            string = string.substring(0,start) 
                                + '<span id="annot-'+m+'" rel="popover" data-color="'+label+'">'
                                + '<span class="label ' + label + '" style="cursor:hand;cursor:pointer;" >'
                                + string.substring(start,end) + '</span></span>' + string.substring(end,string.length+1); 
                            lastMaxIndex = start;
                            currentAnnotationIndex = m;
                            entityMap[currentAnnotationIndex] = [];
                            entityMap[currentAnnotationIndex].push(responseJson.entities[m]);
                        }   */                    
                    } 
                }
//console.log(entityMap);
                string = "<p>" + string.replace(/(\r\n|\n|\r)/gm, "</p><p>") + "</p>";
                //string = string.replace("<p></p>", "");
            
                display += '<td style="font-size:small;width:60%;border:1px solid #CCC;"><p>'+string+'</p></td>';
                display += '<td style="font-size:small;width:40%;padding:0 5px; border:0"><span id="detailed_annot-0" /></td>'; 

                display += '</tr>';
            

            display += '</table>\n';
        }






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



        /*var selected = $('#selectedService option:selected').attr('value');
        var display = "<pre class='prettyprint lang-xml' id='xmlCode'>";  
        var testStr = vkbeautify.xml(responseText);
        
        display += htmll(testStr);

        display += "</pre>";
        $('#requestResult').html(display);
        window.prettyPrint && prettyPrint();*/

        $('#requestResult').show();
    }
    
    $(document).ready(function() {
        $(document).on('shown', '#xmlCode', function(event) {
            prettyPrint();
        });
    });
    
    function processChange() {
        var selected = $('#selectedService option:selected').attr('value');

        if (selected == 'processQuantityText') {
            createInputTextArea('text');
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

    function createInputTextArea(nameInput) {
        //$('#label').html('&nbsp;'); 
        $('#fileInputDiv').hide();
        //$('#input').remove();
        
        //$('#field').html('<table><tr><td><textarea class="span7" rows="5" id="input" name="'+nameInput+'" /></td>'+
        //"<td><span style='padding-left:20px;'>&nbsp;</span></td></tr></table>");
        $('#textInputArea').attr('name', nameInput);
        $('#textInputDiv').show();
        
        $('#gbdForm').attr('enctype', '');
        $('#gbdForm').attr('method', 'post');
    }

        
})(jQuery);



