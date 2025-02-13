#!/usr/bin/env Rscript

# Get command line args
args <- commandArgs(trailingOnly = TRUE)
version <- if (length(args) > 0) args[1] else "v5"

# Ensure required packages are installed
if (!requireNamespace("bookdown", quietly = TRUE)) install.packages("bookdown")
if (!requireNamespace("rmarkdown", quietly = TRUE)) install.packages("rmarkdown")

# Read sidebar and get files
sidebar <- readLines("../_sidebar.md")
markdown_files <- grep("*.md)", sidebar, value = TRUE)
markdown_files <- gsub(".*\\((.+)\\)", "\\1", markdown_files)
markdown_files <- gsub("#.*$", "", markdown_files)

# Create index.Rmd
writeLines(c(
  '---',
  sprintf('title: "Field Book Documentation %s"', version),
  'author: "Field Book Team"',
  'date: "`r Sys.Date()`"',
  'site: bookdown::bookdown_site',
  'documentclass: book',
  'classoption: openany',
  'output:',
  '  bookdown::pdf_book:',
  '    toc: true',
  '    toc_depth: 3',
  '    highlight: tango',
  '    latex_engine: xelatex',
  '    number_sections: false',
  '    includes:',
  '      in_header: preamble.tex',
  '    pandoc_args:',
  '      - "--from"',
  '      - "markdown+raw_html+raw_tex+native_divs+native_spans"',
  '      - "--wrap=none"',
  '      - "--html-q-tags"',
  '---',
  '',
  '\\frontmatter',
  '',
  '# Field Book Documentation',
  '',
  '\\begin{center}',
  '\\includegraphics[width=0.5\\textwidth]{../_static/icons/icon.png}',
  '\\end{center}',
  '',
  '\\mainmatter'
), "index.Rmd")

# Create minimal preamble.tex
writeLines(c(
  '\\usepackage{graphicx}',
  '\\usepackage{hyperref}',
  '\\usepackage[margin=1in]{geometry}',
  '',
  '% Graphics paths',
  '\\graphicspath{',
  '    {../}',
  '    {../_static/}',
  '    {../_static/icons/}',
  '    {../_static/icons/formats/}',
  '    {../_static/images/}',
  '}',
  '',
  '% Hyperlink settings',
  '\\hypersetup{',
  '    colorlinks=true,',
  '    linkcolor=blue,',
  '    filecolor=magenta,',
  '    urlcolor=cyan,',
  '}'
), "preamble.tex")

# Create _bookdown.yml
writeLines(c(
  sprintf('book_filename: "FieldBook_%s_docs"', version),
  sprintf('rmd_files: ["index.Rmd", %s]', paste(sprintf('"%s"', file.path("..", markdown_files)), collapse = ", ")),
  'output_dir: "../_static/pdfs"'
), "_bookdown.yml")

# Build the book
bookdown::render_book("index.Rmd")

# Print success message
cat("PDF documentation built successfully!\n")