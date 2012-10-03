% V2.2
% rbnb 0 duration request test.
% This only works with releases that support at-or-before 0 duration requests.

function [status] = testzerod(host,nframes,fpoints,interval)

    if (nargin < 1)
	host = 'localhost';
    end

    if (nargin < 2)
	nframes = 100;
    end

    if (nargin < 3)
	fpoints = 2;
    end

    if (nargin < 4)
	interval = .75;
    end

    if (nframes < 500)
	cframes = nframes;
	arcmode = 'none';
	aframes = 0;
    else
	cframes = 100;
	arcmode = 'create';
	aframes = nframes;
    end

    src = rbnb_source(host,'mySource',cframes,arcmode,aframes);
    cmapsrc = com.rbnb.sapi.ChannelMap;
    cmapsrc.Add('c1');

    for idx = 1:nframes
	times = ((idx - 1)*fpoints + 1):(idx*fpoints);
	points = -times;
	cmapsrc.PutTimes(times);
	cmapsrc.PutDataAsFloat64(0,points);
	src.Flush(cmapsrc,true);
	etime = times(length(times));
    end

    if (strcmp(arcmode,'create'))
	src.CloseRBNBConnection(false,true);
	src = rbnb_source(host,'mySource',cframes,'append',aframes);
    end

    snk = com.rbnb.sapi.Sink;
    snk.OpenRBNBConnection(host,'mySink');
    cmapsnk = com.rbnb.sapi.ChannelMap;
    cmapsnk.Add(strcat(char(src.GetClientName),'/c1'));

    ntimes = ceil(etime/interval);
    
    for idx=1:ntimes
	rtime = idx*interval;
	atime = floor(rtime);
	adata = -atime;
	snk.Request(cmapsnk,rtime,0.,'absolute');
	rmap = snk.Fetch(10000);
	if (rmap.GetIfFetchTimedOut)
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Failed to get data');
	end
	if (rmap.NumberOfChannels ~= 1)
	    if (atime == 0)
		continue;
	    end
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Failed to get correct channel');
	end
	times = rmap.GetTimes(0);
	data = rmap.GetDataAsFloat64(0);
	if (length(times) ~= 1)
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Wrong amount of data retrieved');
	end
	if (times(1) ~= atime)
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Wrong time retrieved');
	end
	if (data(1) ~= adata)
	    src.CloseRBNBConnection(false,false);
	    snk.CloseRBNBConnection;
	    error('FAIL: Wrong data retrieved');
	end
    end

    src.CloseRBNBConnection(false,false);
    snk.CloseRBNBConnection;

    fprintf('PASS: zero duration request test\n');
return
