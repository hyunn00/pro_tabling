{
    let myClassName = document.currentScript.getAttribute('slider');
    const wrapper = document.querySelector('.'+myClassName +' .wrapper');
    const items = document.querySelector('.'+myClassName +' .wrapper .items');
    const item = document.querySelectorAll('.'+myClassName +' .wrapper .items .reviewT');
    const next = document.querySelector('.'+myClassName +' .next');
    const prev = document.querySelector('.'+myClassName +' .prev');
    const itemCount = 9;

    let startX = 0;         //mousedown시 위치
    let moveX = 0;         //움직인 정도
    let currentIdx = 0;    //현재 위치(index)
    let positions = [];

    function initializeData() {
        const isActive = items.classList.contains('active');
        if (isActive) items.classList.remove('active');
        const width = wrapper.clientWidth;
        const interval = 1008 / 4;//item[1].clientWidth;
        const margin = (width - interval) / 2
        const initX = 0;//Math.floor((interval - margin) * -1);
        let pos = [];
        for (let i = 0; i < itemCount; i++) {
            pos.push(initX - interval * i);
        }
        positions = pos;
    }

    window.addEventListener('resize', initializeData);
    window.addEventListener('load', initializeData);

// btn click event
    next.addEventListener('click', (e) => {
        if (currentIdx === itemCount - 1) return;
        const isActive = items.classList.contains('active');
        if (!isActive) items.classList.add('active');
        currentIdx = currentIdx + 1;
        items.style.left = positions[currentIdx] + 'px';
    });
    prev.addEventListener('click', (e) => {
        if (currentIdx === 0) return;
        const isActive = items.classList.contains('active');
        if (!isActive) items.classList.add('active');
        currentIdx = currentIdx - 1;
        items.style.left = positions[currentIdx] + 'px';
    });


    wrapper.onmousedown = (e) => {
        const rect = wrapper.getBoundingClientRect();
        startX = e.clientX - rect.left;
        const isActive = items.classList.contains('active');
        if (!isActive) items.classList.add('active');
        items.addEventListener('mousemove', onMouseMove);

        document.onmouseup = (e) => {
            console.log("마우스 뗌")
            if (wrapper.classList.contains('active')) wrapper.classList.remove('active');
            items.removeEventListener('mousemove', onMouseMove);
            document.onmouseup = null;
            if (moveX > -70 && moveX <= 70) {
                //   만약 -70~70이면 초기위치로 이동
                return items.style.left = positions[currentIdx] + 'px';
            }
            if (moveX > 0 && currentIdx > 0) {
                currentIdx = currentIdx - 1;
                items.style.left = positions[currentIdx] + 'px';
            }
            if (moveX < 0 && currentIdx < itemCount - 1) {
                currentIdx = currentIdx + 1;
                items.style.left = positions[currentIdx] + 'px';
            }

        }
    }
    function onMouseUp(e) {
        if (wrapper.classList.contains('active')) wrapper.classList.remove('active');
        items.removeEventListener('mousemove', onMouseMove);

        // document에서 이벤트 리스너 제거
        document.removeEventListener('mouseup', onMouseUp);

        if (moveX > -70 && moveX <= 70) {
            //   만약 -70~70이면 초기위치로 이동
            return items.style.left = positions[currentIdx] + 'px';
        }
        if (moveX > 0 && currentIdx > 0) {
            currentIdx = currentIdx - 1;
            items.style.left = positions[currentIdx] + 'px';
        }
        if (moveX < 0 && currentIdx < itemCount - 1) {
            currentIdx = currentIdx + 1;
            items.style.left = positions[currentIdx] + 'px';
        }
    }
    function onMouseMove(e) {
        if (!wrapper.classList.contains('active')) wrapper.classList.add('active');
        const rect = wrapper.getBoundingClientRect();
        moveX = e.clientX - rect.left - startX;
        const left = positions[currentIdx] + moveX;
        if (currentIdx === 0 && moveX > 0) return;
        else if (currentIdx === itemCount - 1 && moveX < 0) return;
        items.style.left = left + 'px';
    }
}