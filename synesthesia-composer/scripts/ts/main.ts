/// <reference path="../../typings/index.d.ts"/>

// Elements
function fileInput() {
  return <HTMLInputElement> $('#file_picker').get(0);
}

function audio() {
  return <HTMLAudioElement> $('#audio').get(0);
}

export function start(): void {
  console.log("start")
  console.log($);

  $('#file_picker').change(loadAudioFile);

}

function loadAudioFile() {
  const file = fileInput().files[0];
  console.debug('file', file);
  audio().src = URL.createObjectURL(document.getElementsByTagName('input')[0].files[0]);
}
