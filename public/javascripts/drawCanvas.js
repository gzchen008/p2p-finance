/**
 * Created by libaozhong on 2015/5/7.
 */

function draw_small(id,num,scaleX,scaleY){
    drawPercent_small(id,num,scaleX,scaleY);
}

function drawPercent_small(id,percent,scaleX,scaleY){
    if(arguments.length == 2){
        var p = percent;
        var c=document.getElementById(id);
        var ctx=c.getContext("2d");
        //ctx.fillStyle="#FF0000";
        ctx.strokeStyle = "#e6e6e6";
        ctx.lineWidth = 6;
        ctx.beginPath();
        ctx.arc(32,32,28,0,Math.PI*2,true);
        ctx.closePath();
        ctx.stroke();

        //var ctx2 = c.getContext("2d");
        ctx.strokeStyle = "rgb(231,76,60)";
        ctx.lineWidth = 6;
        ctx.beginPath();
        if(percent==1){
            p = 0;
        }
        if(percent==0){
            p = 1;
        }
        ctx.shadowBlur=0;
        ctx.shadowColor="rgb(231,76,60)";
        ctx.arc(32,32,28,Math.PI*1.5,Math.PI*(2*(p)-0.5));
        ctx.stroke();

        //var ctx3 = c.getContext("2d");
        ctx.fillStyle = "#fff";
        ctx.beginPath();
        ctx.arc(32,32,26,0,Math.PI*2,true);
        ctx.closePath();
        ctx.fill();

        //var ctx4 = c.getContext("2d");
        ctx.fillStyle = "#646464";
        ctx.font="14px Verdana";
        ctx.textAlign="center";
        ctx.fillText((percent*100)+"%",32,38);
    }
    if(arguments.length == 4){
        var p = percent;
        var c=document.getElementById(id);
        var ctx=c.getContext("2d");
        ctx.scale(scaleX,scaleY);
        //ctx.fillStyle="#FF0000";
        ctx.strokeStyle = "#e6e6e6";
        ctx.lineWidth = 6;
        ctx.beginPath();
        ctx.arc(32,32,28,0,Math.PI*2,true);
        ctx.closePath();
        ctx.stroke();

        //var ctx2 = c.getContext("2d");
        ctx.strokeStyle = "rgb(231,76,60)";
        ctx.lineWidth = 6;
        ctx.beginPath();
        if(percent==1){
            p = 0;
        }
        if(percent==0){
            p = 1;
        }
        ctx.shadowBlur=0;
        ctx.shadowColor="rgb(231,76,60)";
        ctx.arc(32,32,28,Math.PI*1.5,Math.PI*(2*(p)-0.5));
        ctx.stroke();

        //var ctx3 = c.getContext("2d");
        ctx.fillStyle = "#fff";
        ctx.beginPath();
        ctx.arc(32,32,26,0,Math.PI*2,true);
        ctx.closePath();
        ctx.fill();

        //var ctx4 = c.getContext("2d");
        ctx.fillStyle = "#646464";
        ctx.font="14px Verdana";
        ctx.textAlign="center";
        ctx.fillText((percent*100)+"%",32,38);
    }
}
