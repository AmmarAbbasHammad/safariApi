<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
    <body>
        <form name ="frm_upload" method="POST" action='WaselDriver'enctype="multipart/form-data">
            <input type="hidden" name="action" id="action" value="UploadProFile.php" />
            <ul>
                <li><label>رقم العطاء</label>
                    <input type="text" name="DriverID" id ="DriverID"
                           value="1" />
                </li>
                <li> <label>صورة العرض960*360px</label>
                    <input type="file" name="ImageProFile" id="ImageProFile" accept="image/*" onchange="loadFile(event);">
                    <img id="output"/>
                    <script>
                        var loadFile = function(event) {
                            var reader = new FileReader();
                            reader.onload = function() {
                                var output = document.getElementById('output');
                                output.src = reader.result;
                            };
                            reader.readAsDataURL(event.target.files[0]);
                        };
                    </script></li>

                <li>
                    <input type="submit" value="رفع " />
                </li>
            </ul>
        </form>
    </body>
</html>