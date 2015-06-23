<div class="panel-body" id="roles-settings">
   <form method="POST" action="/graphs/updateRoles" enctype="multipart/form-data">
       Mediators percentage
       <input value="${mediatorsPer}" type="range" name="mediator"  min="0" max="100" step=1 required oninput="outputUpdateMediators(value)">
       <output for=fader id=mediators>${mediatorsPer} %</output><br/><br/>
       Influential percentage
       <input value="${influentialPer}" type="range" name="influential" min="0" max="100" step=1 required oninput="outputUpdateInfluentials(value)">
       <output for=fader id=influentials>${influentialPer} %</output><br/><br/>
       <input class="btn btn-primary" type="submit" value="Update Settings">
   </form>
    <script>
        function outputUpdateMediators(vol) {
            document.querySelector('#mediators').value = vol + ' %';
        }

        function outputUpdateInfluentials(vol) {
            document.querySelector('#influentials').value = vol + ' %';
        }
    </script>
</div>
