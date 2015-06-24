/*
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package com.klinker.android.link_builder;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.MovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LinkBuilder {

    private static final String TAG = "LinkBuilder";

    private TextView textView;
    private List<Link> links = new ArrayList<>();

    private SpannableString spannable = null;

    /**
     * Construct a LinkBuilder object.
     * @param textView The TextView you will be adding links to.
     */
    public LinkBuilder(TextView textView) {
        this.textView = textView;
    }

    /**
     * Add a single link to the builder.
     * @param link the rule that you want to link with.
     */
    public void addLink(Link link) {
        this.links.add(link);
    }

    /**
     * Add a list of links to the builder.
     * @param links list of rules you want to link with.
     */
    public void addLinks(List<Link> links) {
        this.links.addAll(links);
    }

    /**
     * Execute the rules to create the linked text.
     */
    public void build() {
        // exit if there are no links
        if (links.size() == 0) {
            return;
        }

        // we extract individual links from the patterns
        turnPatternsToLinks();

        // add those links to our spannable text so they can be clicked
        for (Link link : links) {
            addLinkToSpan(link);
        }

        // set the spannable text
        textView.setText(spannable);

        // add the movement method so we know what actions to perform on the clicks
        addLinkMovementMethod();
    }

    /**
     * Add the link rule and check if spannable text is created.
     * @param link rule to add to the text.
     */
    private void addLinkToSpan(Link link) {
        // create a new spannable string if none exists
        if (spannable == null) {
            spannable = SpannableString.valueOf(textView.getText());
        }

        // add the rule to the spannable string
        addLinkToSpan(spannable, link);
    }

    /**
     * Find the link within the spannable text
     * @param s spannable text that we are adding the rule to.
     * @param link rule to add to the text.
     */
    private void addLinkToSpan(Spannable s, Link link) {
        // get the current text
        String text = textView.getText().toString();

        // find the start and end point of the linked text within the TextView
        int start = text.indexOf(link.getText());
        if (start >= 0) {
            int end = start + link.getText().length();

            // add link to the spannable text
            applyLink(link, new Range(start, end), s);
        }

    }

    /**
     * Add the movement method to handle the clicks.
     */
    private void addLinkMovementMethod() {
        MovementMethod m = textView.getMovementMethod();

        if ((m == null) || !(m instanceof TouchableMovementMethod)) {
            if (textView.getLinksClickable()) {
                textView.setMovementMethod(TouchableMovementMethod.getInstance());
            }
        }
    }

    /**
     * Set the link rule to the spannable text.
     * @param link rule we are applying.
     * @param range the start and end point of the link within the text.
     * @param text the spannable text to add the link to.
     */
    private void applyLink(Link link, final Range range, final Spannable text) {
        TouchableSpan span = new TouchableSpan(link);
        text.setSpan(span, range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * Find the links that contain patterns and convert them to individual links.
     */
    private void turnPatternsToLinks() {
        int size = links.size();
        int i = 0;
        while (i < size) {
            if (links.get(i).getPattern() != null) {
                addLinksFromPattern(links.get(i));

                links.remove(i);
                size--;
            } else {
                i++;
            }
        }
    }

    /**
     * Convert the pattern to individual links.
     * @param linkWithPattern pattern we want to match.
     */
    private void addLinksFromPattern(Link linkWithPattern) {
        String text = textView.getText().toString();
        Pattern pattern = linkWithPattern.getPattern();
        Matcher m = pattern.matcher(text);

        while (m.find()) {
            links.add(new Link(linkWithPattern).setText(m.group()));
        }
    }

    /**
     * Manages the start and end points of the linked text.
     */
    private static class Range {
        public int start;
        public int end;

        public Range(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}