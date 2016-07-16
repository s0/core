var gulp = require('gulp');
var bower = require('gulp-bower');
var sourcemaps = require('gulp-sourcemaps');
var ts = require('gulp-typescript');

var tsProject = ts.createProject('scripts/ts/tsconfig.json');

gulp.task('bower', function() {
  return bower();
});

gulp.task('ts', function () {
    return tsProject.src()
      .pipe(sourcemaps.init())
      .pipe(ts(tsProject))
      .pipe(sourcemaps.write())
      .pipe(gulp.dest('scripts/js'));
});

gulp.task('default', ['bower', 'ts']);
