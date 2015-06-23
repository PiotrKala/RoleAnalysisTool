<div id="openUploadModal" class="modalDialog">
    <div>
        <a href="#close" title="Close" class="close">X</a>
        <div class="panel-body">
            <form method="POST" action="/graphs/upload" enctype="multipart/form-data">
                <input type="file" name="file"><br/>
                <input class="btn btn-primary btn-lg" type="submit" value="Upload">
            </form>
        </div>
    </div>
</div>